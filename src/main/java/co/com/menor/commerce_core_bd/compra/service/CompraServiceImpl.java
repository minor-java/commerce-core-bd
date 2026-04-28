package co.com.menor.commerce_core_bd.compra.service;

import co.com.menor.commerce_core_bd.compra.mapper.CompraMapper;
import co.com.menor.commerce_core_bd.compra.mapper.CompraResponseMapper;
import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.commerce_core_bd.compra.repository.CompraDetalleRepository;
import co.com.menor.commerce_core_bd.compra.repository.CompraRepository;
import co.com.menor.commerce_core_bd.compra.repository.CompraSpecification;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleByIdResponse;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.commerce_core_bd.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository compraDetalleRepository;
    private final CompraMapper compraMapper;
    private final CompraResponseMapper compraResponseMapper;
    private final InventarioService inventarioService;

    @Override
    @Transactional
    public CompraResponse crearCompra(CompraRequest req) {
       
        if (req == null) {
            throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        }
        
        if (req.getDetalles() == null || req.getDetalles().isEmpty()) {
            throw new MinorExcepcion("DETALLES_REQUERIDOS",
                    "La compra debe contener al menos un detalle");
        }

        Compra compra = compraMapper.toEntity(req);
        Compra compraGuardada = compraRepository.save(compra);
        log.info("crearCompra: compra guardada id={}", compraGuardada.getId());

        List<CompraDetalle> detalles = req.getDetalles().stream()
                .map(d -> compraMapper.toDetalleEntity(d, compraGuardada.getId(), req.getCreadoPor()))
                .collect(Collectors.toList());

        // saveAll retorna las entidades con sus IDs generados
        List<CompraDetalle> detallesGuardados = compraDetalleRepository.saveAll(detalles);
        log.info("crearCompra: {} detalles guardados para compraId={}", detallesGuardados.size(), compraGuardada.getId());

        // Registrar movimiento de inventario por cada detalle.
        // Si cualquier registro falla lanza excepción y @Transactional revierte
        // la compra y todos los detalles ya guardados en esta misma transacción.
        detallesGuardados.forEach(detalle -> {
            CreateMovimientoInventarioRequest movReq = new CreateMovimientoInventarioRequest();
            movReq.setProductoId(detalle.getProductoId());
            movReq.setTipo("ENTRADA");
            movReq.setCantidad(detalle.getCantidad());
            movReq.setCostoUnitario(detalle.getCostoUnitario());
            movReq.setReferenciaTipo("COMPRA_DETALLE");
            movReq.setReferenciaId(detalle.getId());
            movReq.setCreadoPor(req.getCreadoPor());

            inventarioService.registrarMovimiento(movReq);

            log.info("crearCompra: movimiento registrado productoId={} compraDetalleId={}",
                    detalle.getProductoId(), detalle.getId());
        });

        BigDecimal total = detallesGuardados.stream()
                .map(CompraDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        compraGuardada.setTotal(total);
        compraRepository.save(compraGuardada);

        return compraResponseMapper.toResponse(compraGuardada, detallesGuardados);
    }

    @Override
    public List<CompraResponse> obtenerTodas() {
        return compraResponseMapper.toResponseList(compraRepository.findAll());
    }

    @Override
    public List<CompraResponse> obtenerTodasConDetalles() {
        return compraRepository.findAll().stream()
        .map(c -> compraResponseMapper.toResponse(
                c,
                compraDetalleRepository.findByCompraId(c.getId())
        ))
        .collect(Collectors.toList());
    }

    @Override
    public CompraDetalleByIdResponse obtenerDetallePorId(Long id) {
        CompraDetalle detalle = compraDetalleRepository.findById(id)
                .orElseThrow(() -> new MinorExcepcion("DETALLE_NO_ENCONTRADO", "No existe un detalle con id: " + id));
        Compra compra = compraRepository.findById(detalle.getCompraId())
                .orElseThrow(() -> new MinorExcepcion("COMPRA_NO_ENCONTRADA", "No existe la compra asociada con id: " + detalle.getCompraId()));
        return compraResponseMapper.toDetalleByIdResponse(detalle, compra);
    }

    @Override
    public CompraResponse obtenerPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new MinorExcepcion("COMPRA_NO_ENCONTRADA", "No existe una compra con id: " + id));
        List<CompraDetalle> detalles = compraDetalleRepository.findByCompraId(id);
        return compraResponseMapper.toResponse(compra, detalles);
    }

    @Override
    public Page<CompraResponse> buscarDetalladas(FiltroCompraRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Page<Compra> page = compraRepository.findAll(CompraSpecification.buildFrom(filtro), pageable);
        return page.map(c -> compraResponseMapper.toResponse(
            c,
            compraDetalleRepository.findByCompraId(c.getId())
        ));
    }
}
