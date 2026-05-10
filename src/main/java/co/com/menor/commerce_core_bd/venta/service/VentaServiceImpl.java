package co.com.menor.commerce_core_bd.venta.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.caja.service.CajaService;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.StockActual;
import co.com.menor.commerce_core_bd.movimiento.repository.MovimientoCajaRepository;
import co.com.menor.commerce_core_bd.movimiento.service.MovimientoService;
import co.com.menor.commerce_core_bd.movimiento.service.StockActualService;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.venta.mapper.VentaMapper;
import co.com.menor.commerce_core_bd.venta.mapper.VentaResponseMapper;
import co.com.menor.commerce_core_bd.venta.model.Venta;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.repository.VentaRepository;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import co.com.menor.comun_dto.venta.request.FiltroVentaRequest;
import co.com.menor.comun_dto.venta.request.VentaRequest;
import co.com.menor.comun_dto.venta.response.VentaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleService ventaDetalleService;
    
    private final MovimientoService movimientoService;
    
    // private final MovimientoCajaRepository movimientoCajaRepository;
    
    private final VentaMapper ventaMapper;
    private final VentaResponseMapper ventaResponseMapper;
    
    private final StockActualService stockActualService;
    private final CajaService cajaService;

    @Override
    @Transactional
    public VentaResponse crearVenta(VentaRequest req) {
        
        // if (req == null) {
        //     throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        // }
        // if (req.getDetalles() == null || req.getDetalles().isEmpty()) {
        //     throw new MinorExcepcion("DETALLES_REQUERIDOS", "La venta debe contener al menos un detalle");
        // }
        // if (req.getMetodoPago() == null || req.getMetodoPago().trim().isEmpty()) {
        //     throw new MinorExcepcion("METODO_PAGO_REQUERIDO", "El método de pago es obligatorio");
        // }

        // CajaResponse cajaAbierta = cajaService.obtenerPorUsuarioId(req.getUsuarioId());

        // req.getDetalles().forEach(d -> {
           
        //     Optional<StockActual> stockOpt = 
        //     stockActualService.buscarPorProductoId(d.getProductoId());

        //     BigDecimal stockDisponible = stockOpt.map(s -> s.getStock()).orElse(BigDecimal.ZERO);
            
        //     if (stockDisponible.compareTo(d.getCantidad()) < 0) {

        //         log.error("El producto no tiene stock disponible: ", d.getProductoId());

        //         throw new MinorExcepcion(
        //             "ERROR",
        //             "VentaService crearVenta"
        //         );
        //     }
        // });

        Venta venta = ventaMapper.toEntity(req);
        Venta ventaGuardada = ventaRepository.save(venta);
        log.info("crearVenta: venta guardada id={}", ventaGuardada.getId());

        List<VentaDetalle> detalles = req.getDetalles()
        .stream()
        .map(d -> ventaMapper.toDetalleEntity(d, ventaGuardada.getId()))
        .collect(Collectors.toList());

        List<VentaDetalle> detallesGuardados = ventaDetalleService.guardarTodo(detalles);

        detallesGuardados.forEach(detalle -> {
            
            BigDecimal costoUnitario = 
            stockActualService.buscarPorProductoId(detalle.getProductoId())
            .map(s -> s.getCostoPromedio())
                .orElse(BigDecimal.ZERO);

            MovimientoInventario movimiento = new MovimientoInventario();

            movimiento.setProductoId(detalle.getProductoId());
            movimiento.setTipo("SALIDA");
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setCostoUnitario(costoUnitario);
            movimiento.setCostoTotal(detalle.getCantidad().multiply(costoUnitario));
            movimiento.setReferenciaTipo("VENTA_DETALLE");
            movimiento.setReferenciaId(detalle.getId());
            movimiento.setFechaCreacion(LocalDateTime.now());
            movimiento.setUsuarioId(req.getUsuarioId());

            movimientoService.guardarMovimiento(movimiento);
            stockActualService.actualizarStock(movimiento);
        });

        BigDecimal total = detallesGuardados.stream()
        .map(VentaDetalle::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        ventaGuardada.setTotal(total);
        ventaRepository.save(ventaGuardada);

        MovimientoCaja movCaja = new MovimientoCaja();
        movCaja.setCajaId(req.getCajaId());
        movCaja.setTipo("INGRESO");
        movCaja.setMetodoPago(req.getMetodoPago());
        movCaja.setMonto(total);
        movCaja.setReferenciaTipo("VENTA");
        movCaja.setReferenciaId(ventaGuardada.getId());
        movCaja.setFechaCreacion(LocalDateTime.now());
        movCaja.setUsuarioId(req.getUsuarioId());

        movimientoService.guardarMovimientoCaja(movCaja);
        
        log.info("crearVenta: movimiento caja registrado cajaId={} monto={}", req.getCajaId(), total);

        return ventaResponseMapper.toResponse(ventaGuardada, detallesGuardados);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new MinorExcepcion("VENTA_NO_ENCONTRADA",
                        "No existe una venta con id: " + id));
                        
        List<VentaDetalle> detalles = ventaDetalleService.buscarPorVentaId(id);
        return ventaResponseMapper.toResponse(venta, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPaginado(FiltroVentaRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Specification<Venta> spec = buildSpec(filtro);
        return ventaRepository.findAll(spec, pageable)
                .map(v -> ventaResponseMapper.toResponse(v, ventaDetalleService.buscarPorVentaId(v.getId())));
    }

    private Specification<Venta> buildSpec(FiltroVentaRequest filtro) {
        return (root, query, cb) -> {
            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (filtro.getCreadoPor() != null) {
                predicates.add(cb.equal(root.get("creadoPor"), filtro.getCreadoPor()));
            }
            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaDesde()));
            }
            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaHasta()));
            }
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }
}
