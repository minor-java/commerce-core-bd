package co.com.menor.commerce_core_bd.compra.service;

import co.com.menor.commerce_core_bd.compra.mapper.CompraDetalleMapper;
import co.com.menor.commerce_core_bd.compra.mapper.CompraMapper;
import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.commerce_core_bd.compra.repository.CompraRepository;
import co.com.menor.commerce_core_bd.compra.repository.CompraSpecification;
import co.com.menor.commerce_core_bd.movimiento.service.MovimientoService;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.utils.MovimientoInventarioConstants;
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

    private final CompraMapper compraMapper;
    private final CompraDetalleMapper compraDetalleMapper;

    private final MovimientoService inventarioService;
    private final CompreDetalleService compreDetalleService;

    @Override
    @Transactional
    public CompraResponse crearCompra(CompraRequest req) {
       
        try {

            Compra compra = compraMapper.toEntity(req);
            Compra compraGuardada = compraRepository.save(compra);
            log.info("crearCompra: compra guardada id={}", compraGuardada.getId());
    
            List<CompraDetalle> detalles = 
            req.getDetalles()
            .stream()
            .map(d -> compraDetalleMapper.toDetalleEntity(d, compraGuardada.getId(), req.getUsuarioId()))
            .collect(Collectors.toList());
    
            // saveAll retorna las entidades con sus IDs generados
            List<CompraDetalle> detallesGuardados = compreDetalleService.guardarDetalles(detalles);
            log.info("crearCompra: {} detalles guardados para compraId={}", detallesGuardados.size(), compraGuardada.getId());
    
            // Registrar movimiento de inventario por cada detalle.
            // Si cualquier registro falla lanza excepción y @Transactional revierte
            // la compra y todos los detalles ya guardados en esta misma transacción.
            detallesGuardados.forEach(detalle -> {
                CreateMovimientoInventarioRequest movReq = new CreateMovimientoInventarioRequest();
                movReq.setProductoId(detalle.getProductoId());
                movReq.setTipo(MovimientoInventarioConstants.TIPO_ENTRADA);
                movReq.setCantidad(detalle.getCantidad());
                movReq.setCostoUnitario(detalle.getCostoUnitario());
                movReq.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_COMPRA_DETALLE);
                movReq.setReferenciaId(detalle.getId());
                movReq.setUsuarioId(req.getUsuarioId());
    
                inventarioService.registrarMovimiento(movReq);
            });
    
            BigDecimal total = detallesGuardados.stream()
                    .map(CompraDetalle::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    
            compraGuardada.setTotal(total);
            compraRepository.save(compraGuardada);
    
            return compraMapper.toResponse(compraGuardada);

        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "CompraService crearCompra");
        }
    }

    @Override
    public CompraResponse obtenerCompraPorId(Long id) {
        
        try {
            
            Compra compra = compraRepository.findById(id).get();
            return compraMapper.toResponse(compra);
        } catch (Exception e) {

            throw new MinorExcepcion(
                "ERROR",
                "CompraService obtenerCompraPorId"
            );
        }
    }

    @Override
    public Page<CompraResponse> buscarComprasPaginado(FiltroCompraRequest filtro) {
        
        try {
            
            PageRequest pageable = PageRequest.of(
                filtro.getPage(),
                filtro.getSize()
            );
        
            Page<Compra> page = compraRepository.findAll(
                CompraSpecification.buildFrom(filtro),
                pageable
            );
        
            return page.map(compraMapper::toResponse);
            
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CompraService buscarComprasPaginado"
            );
        }
    }
    
}
