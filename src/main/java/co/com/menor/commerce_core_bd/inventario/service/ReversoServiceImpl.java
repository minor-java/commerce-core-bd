package co.com.menor.commerce_core_bd.inventario.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.caja.repository.CajaRepository;
import co.com.menor.commerce_core_bd.caja.repository.MovimientoCajaRepository;
import co.com.menor.commerce_core_bd.inventario.mapper.ReversoMapper;
import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.inventario.model.Reverso;
import co.com.menor.commerce_core_bd.inventario.repository.MovimientoInventarioRepository;
import co.com.menor.commerce_core_bd.inventario.repository.ReversoRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.repository.VentaDetalleRepository;
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

    private final MovimientoInventarioRepository movimientoRepository;
    private final ReversoRepository reversoRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ReversoMapper reversoMapper;
    private final InventarioService inventarioService;

    @Override
    @Transactional
    public ReversoResponse crearReverso(ReversoRequest req) {
        if (req == null) {
            throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        }
        if (req.getMovimientoId() == null || req.getCantidadReversada() == null || req.getCreadoPor() == null) {
            throw new MinorExcepcion("CAMPOS_OBLIGATORIOS_FALTANTES",
                    "Los campos movimientoId, cantidadReversada y creadoPor son obligatorios");
        }
        if (req.getCantidadReversada().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MinorExcepcion("CANTIDAD_INVALIDA", "La cantidad a reversar debe ser mayor a cero");
        }

        MovimientoInventario origen = movimientoRepository.findById(req.getMovimientoId())
                .orElseThrow(() -> new MinorExcepcion("MOVIMIENTO_NO_ENCONTRADO",
                        "No existe un movimiento con id " + req.getMovimientoId()));

        if (REFERENCIA_REVERSO.equalsIgnoreCase(origen.getReferenciaTipo())) {
            throw new MinorExcepcion("REVERSO_DE_REVERSO_NO_PERMITIDO",
                    "No se puede reversar un movimiento que ya es un reverso (id=" + origen.getId() + ")");
        }

        BigDecimal totalReversadoPrevio = reversoRepository.sumCantidadReversadaByMovimientoId(origen.getId());
        BigDecimal disponible = origen.getCantidad().subtract(totalReversadoPrevio);
        if (req.getCantidadReversada().compareTo(disponible) > 0) {
            throw new MinorExcepcion("CANTIDAD_REVERSO_EXCEDE_DISPONIBLE",
                    "La cantidad a reversar (" + req.getCantidadReversada()
                    + ") excede la disponible (" + disponible + ") para el movimiento id=" + origen.getId());
        }

        boolean esReversoDeVenta = REFERENCIA_VENTA_DETALLE.equalsIgnoreCase(origen.getReferenciaTipo());
        if (esReversoDeVenta) {
            cajaRepository.findByCreadoPorAndEstado(req.getCreadoPor(), "ABIERTA")
                    .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ABIERTA",
                            "El usuario " + req.getCreadoPor() + " no tiene una caja abierta"));
        }

        Reverso reverso = reversoMapper.toEntity(req);
        Reverso savedReverso = reversoRepository.save(reverso);
        log.info("crearReverso: reverso id={} movimientoId={} cantidad={}",
                savedReverso.getId(), origen.getId(), req.getCantidadReversada());

        String tipoReverso = "SALIDA".equalsIgnoreCase(origen.getTipo()) ? "ENTRADA" : "SALIDA";
        MovimientoInventario movReverso = new MovimientoInventario();
        movReverso.setProductoId(origen.getProductoId());
        movReverso.setTipo(tipoReverso);
        movReverso.setCantidad(req.getCantidadReversada());
        movReverso.setCostoUnitario(origen.getCostoUnitario());
        movReverso.setCostoTotal(req.getCantidadReversada().multiply(origen.getCostoUnitario()));
        movReverso.setReferenciaTipo(REFERENCIA_REVERSO);
        movReverso.setReferenciaId(savedReverso.getId());
        movReverso.setMovimientoOrigenId(origen.getId());
        movReverso.setFechaCreacion(LocalDateTime.now());
        movReverso.setCreadoPor(req.getCreadoPor());
        movimientoRepository.save(movReverso);

        // Para reverso de venta: el costo promedio NO se recalcula (actualizarStock lo maneja por referenciaTipo)
        inventarioService.actualizarStock(movReverso);

        if (esReversoDeVenta) {
            registrarMovimientoCajaReversoVenta(savedReverso, origen, req);
        }

        return reversoMapper.toResponse(savedReverso);
    }

    private void registrarMovimientoCajaReversoVenta(
            Reverso savedReverso, MovimientoInventario origen, ReversoRequest req) {
        VentaDetalle ventaDetalle = ventaDetalleRepository.findById(origen.getReferenciaId())
                .orElseThrow(() -> new MinorExcepcion("VENTA_DETALLE_NO_ENCONTRADO",
                        "No existe el detalle de venta con id " + origen.getReferenciaId()));

        BigDecimal montoDevolucion = req.getCantidadReversada().multiply(ventaDetalle.getPrecioUnitario());

        co.com.menor.commerce_core_bd.caja.model.Caja cajaAbierta = cajaRepository
                .findByCreadoPorAndEstado(req.getCreadoPor(), "ABIERTA")
                .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ABIERTA",
                        "El usuario " + req.getCreadoPor() + " no tiene una caja abierta"));

        MovimientoCaja movCaja = new MovimientoCaja();
        movCaja.setCajaId(cajaAbierta.getId());
        movCaja.setTipo("EGRESO");
        movCaja.setMetodoPago("EFECTIVO");
        movCaja.setMonto(montoDevolucion);
        movCaja.setReferenciaTipo("REVERSO");
        movCaja.setReferenciaId(savedReverso.getId());
        movCaja.setFechaCreacion(LocalDateTime.now());
        movCaja.setCreadoPor(req.getCreadoPor());
        movimientoCajaRepository.save(movCaja);

        log.info("registrarMovimientoCajaReversoVenta: cajaId={} monto={}", cajaAbierta.getId(), montoDevolucion);
    }
}
