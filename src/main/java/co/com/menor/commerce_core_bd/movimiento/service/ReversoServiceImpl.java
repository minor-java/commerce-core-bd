package co.com.menor.commerce_core_bd.movimiento.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.caja.service.CajaService;
import co.com.menor.commerce_core_bd.movimiento.mapper.ReversoMapper;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.Reverso;
import co.com.menor.commerce_core_bd.movimiento.repository.ReversoRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.service.VentaDetalleService;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReversoServiceImpl implements ReversoService {

    private static final String REFERENCIA_VENTA_DETALLE = "VENTA_DETALLE";
    private static final String REFERENCIA_REVERSO = "REVERSO";

    private final ReversoRepository reversoRepository;

    private final ReversoMapper reversoMapper;

    private final StockActualService stockActualService;
    private final VentaDetalleService ventaDetalleService;
    private final MovimientoService movimientoService;

    @Override
    @Transactional
    public ReversoResponse crearReverso(ReversoRequest req) {
        
        // if (req == null) {
        //     throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        // }
        // if (req.getMovimientoId() == null || req.getCantidadReversada() == null || req.getUsuarioId() == null) {
        //     throw new MinorExcepcion("CAMPOS_OBLIGATORIOS_FALTANTES",
        //             "Los campos movimientoId, cantidadReversada y creadoPor son obligatorios");
        // }
        // if (req.getCantidadReversada().compareTo(BigDecimal.ZERO) <= 0) {
        //     throw new MinorExcepcion("CANTIDAD_INVALIDA", "La cantidad a reversar debe ser mayor a cero");
        // }
        // MovimientoInventario origen = movimientoRepository.findById(req.getMovimientoId())
        //         .orElseThrow(() -> new MinorExcepcion("MOVIMIENTO_NO_ENCONTRADO",
        //                 "No existe un movimiento con id " + req.getMovimientoId()));

        // if (REFERENCIA_REVERSO.equalsIgnoreCase(origen.getReferenciaTipo())) {
        //     throw new MinorExcepcion("REVERSO_DE_REVERSO_NO_PERMITIDO",
        //             "No se puede reversar un movimiento que ya es un reverso (id=" + origen.getId() + ")");
        // }

        // BigDecimal totalReversadoPrevio = reversoRepository.sumCantidadReversadaByMovimientoId(origen.getId());
        // BigDecimal disponible = origen.getCantidad().subtract(totalReversadoPrevio);
        
        // if (req.getCantidadReversada().compareTo(disponible) > 0) {
        //     throw new MinorExcepcion("CANTIDAD_REVERSO_EXCEDE_DISPONIBLE",
        //             "La cantidad a reversar (" + req.getCantidadReversada()
        //             + ") excede la disponible (" + disponible + ") para el movimiento id=" + origen.getId());
        // }

        // boolean esReversoDeVenta = REFERENCIA_VENTA_DETALLE.equalsIgnoreCase(origen.getReferenciaTipo());

        // if (esReversoDeVenta) {
        //     cajaRepository.findByUsuarioIdAndEstado(req.getUsuarioId(), "ABIERTA")
        //             .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ABIERTA",
        //                     "El usuario " + req.getUsuarioId() + " no tiene una caja abierta"));
        // }

        try {
            
            MovimientoInventario movimiento = movimientoService.buscarPorId(req.getMovimientoId()).get();
    
            Reverso reverso = reversoMapper.toEntity(req);
            Reverso reversoGuardado = reversoRepository.save(reverso);
    
            log.info(
                "crearReverso: reverso id={} movimientoId={} cantidad={}",
                reversoGuardado.getId(), movimiento.getId(), req.getCantidadReversada()
            );
    
            String tipoReverso = "SALIDA".equalsIgnoreCase(movimiento.getTipo()) ? "ENTRADA" : "SALIDA";
            MovimientoInventario movReverso = new MovimientoInventario();
            
            movReverso.setProductoId(movimiento.getProductoId());
            movReverso.setTipo(tipoReverso);
            movReverso.setCantidad(req.getCantidadReversada());
            movReverso.setCostoUnitario(movimiento.getCostoUnitario());
            movReverso.setCostoTotal(req.getCantidadReversada().multiply(movimiento.getCostoUnitario()));
            movReverso.setReferenciaTipo(REFERENCIA_REVERSO);
            movReverso.setReferenciaId(reversoGuardado.getId());
            movReverso.setMovimientoOrigenId(movimiento.getId());
            movReverso.setFechaCreacion(LocalDateTime.now());
            movReverso.setUsuarioId(req.getUsuarioId());
    
            movimientoService.guardarMovimiento(movReverso);
    
            // Para reverso de venta: el costo promedio NO se recalcula (actualizarStock lo maneja por referenciaTipo)
            stockActualService.actualizarStock(movReverso);
            boolean esReversoDeVenta = REFERENCIA_VENTA_DETALLE.equalsIgnoreCase(movimiento.getReferenciaTipo());
    
            if (esReversoDeVenta) {
                registrarMovimientoCajaReversoVenta(
                    reversoGuardado.getId(), 
                    movimiento.getReferenciaId(), 
                    req.getUsuarioId(),
                    req.getCantidadReversada(),
                    req.getCajaId()
                );
            }
    
            return reversoMapper.toResponse(reversoGuardado);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "crearReverso ReversoService"
            );
        }
    }

    private void registrarMovimientoCajaReversoVenta(
        Long reversoId, 
        Long referenciaId, 
        Long usuarioId,
        BigDecimal cantidadReversada,
        Long cajaId
    ) {

        // VentaDetalle ventaDetalle = ventaDetalleRepository.findById(referenciaId)
        //         .orElseThrow(() -> new MinorExcepcion("VENTA_DETALLE_NO_ENCONTRADO",
        //                 "No existe el detalle de venta con id " + referenciaId));

        // Caja cajaAbierta = cajaRepository
        //         .findByUsuarioIdAndEstado(usuarioId, "ABIERTA")
        //         .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ABIERTA",
        //                 "El usuario " + usuarioId + " no tiene una caja abierta"));
        // CajaResponse caja = cajaService.obtenerPorUsuarioId(usuarioId);

        VentaDetalle ventaDetalle = ventaDetalleService.buscarPorId(referenciaId).get();
        BigDecimal montoDevolucion = cantidadReversada.multiply(ventaDetalle.getPrecioUnitario());

        MovimientoCaja movCaja = new MovimientoCaja();
        movCaja.setCajaId(cajaId);
        movCaja.setTipo("EGRESO");
        movCaja.setMetodoPago("EFECTIVO");
        movCaja.setMonto(montoDevolucion);
        movCaja.setReferenciaTipo("REVERSO");
        movCaja.setReferenciaId(reversoId);
        movCaja.setFechaCreacion(LocalDateTime.now());
        movCaja.setUsuarioId(usuarioId);

        movimientoService.guardarMovimientoCaja(movCaja);

        log.info("registrarMovimientoCajaReversoVenta: cajaId={} monto={}", cajaId, montoDevolucion);
    }
}
