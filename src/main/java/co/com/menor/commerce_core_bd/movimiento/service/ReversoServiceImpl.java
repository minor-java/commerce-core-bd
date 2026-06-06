package co.com.menor.commerce_core_bd.movimiento.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.movimiento.mapper.ReversoMapper;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.Reverso;
import co.com.menor.commerce_core_bd.movimiento.repository.MovimientoCajaRepository;
import co.com.menor.commerce_core_bd.movimiento.repository.ReversoRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.service.VentaDetalleService;
import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.request.VentaDetalleReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;
import co.com.menor.comun_dto.utils.CajaConstants;
import co.com.menor.comun_dto.utils.MovimientoInventarioConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReversoServiceImpl implements ReversoService {

    private final ReversoRepository reversoRepository;
    private final ReversoMapper reversoMapper;
    private final StockActualService stockActualService;
    private final VentaDetalleService ventaDetalleService;
    private final MovimientoService movimientoService;
    private final MovimientoCajaRepository movimientoCajaRepository;

    // -----------------------------------------------------------------------
    // crearReverso — flujo MOVIMIENTOS (recibe movimientoId)
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public ReversoResponse crearReverso(ReversoRequest req) {

        try {
            MovimientoInventario movimiento = movimientoService.buscarPorId(req.getMovimientoId())
                    .orElseThrow(() -> new MinorExcepcion("MOVIMIENTO_NO_ENCONTRADO",
                            "No existe el movimiento id=" + req.getMovimientoId()));

            if (MovimientoInventarioConstants.REFERENCIA_REVERSO
                    .equalsIgnoreCase(movimiento.getReferenciaTipo())) {
                throw new MinorExcepcion("REVERSO_NO_PERMITIDO",
                        "No se puede reversar un movimiento que ya es un reverso (id=" + movimiento.getId() + ")");
            }

            // Detección de combo: si el movimiento es VENTA_DETALLE y el detalle tiene comboId
            if (MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE
                    .equalsIgnoreCase(movimiento.getReferenciaTipo())) {
                VentaDetalle vd = ventaDetalleService.buscarPorId(movimiento.getReferenciaId())
                        .orElse(null);
                if (vd != null && vd.getComboId() != null) {
                    log.info("crearReverso: movimiento id={} pertenece a combo, reversando combo completo", movimiento.getId());
                    return reversarComboCompleto(vd, req.getUsuarioId(), req.getCajaId(), req.getObservacion());
                }
            }

            // Reverso individual
            return reversarMovimientoIndividual(movimiento, req.getCantidadReversada(),
                    req.getObservacion(), req.getUsuarioId(), req.getCajaId());

        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            log.error("crearReverso error", e);
            throw new MinorExcepcion("ERROR", "crearReverso ReversoService");
        }
    }

    // -----------------------------------------------------------------------
    // crearReversoVentaDetalle — flujo CAJA / DEVOLUCIONES (recibe ventaDetalleId)
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public ReversoResponse crearReversoVentaDetalle(VentaDetalleReversoRequest req) {

        VentaDetalle vd = ventaDetalleService.buscarPorId(req.getVentaDetalleId())
                .orElseThrow(() -> new MinorExcepcion("VD_NO_ENCONTRADO",
                        "No existe el detalle de venta id=" + req.getVentaDetalleId()));

        if (vd.getComboId() != null) {
            log.info("crearReversoVentaDetalle: ventaDetalleId={} es combo, procesando reverso combo", vd.getId());
            return crearReversoVentaDetalleCombo(vd, req);
        } else {
            return crearReversoVentaDetalleIndividual(vd, req);
        }
    }

    // -----------------------------------------------------------------------
    // Reverso individual para CAJA/DEVOLUCIONES
    // -----------------------------------------------------------------------

    private ReversoResponse crearReversoVentaDetalleIndividual(VentaDetalle vd, VentaDetalleReversoRequest req) {

        List<MovimientoInventario> movs = movimientoService.buscarTodosPorReferenciaYTipo(
                MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE, vd.getId());

        if (movs.isEmpty()) {
            throw new MinorExcepcion("MOVIMIENTO_NO_ENCONTRADO",
                    "No existe movimiento para el detalle de venta id=" + vd.getId());
        }

        MovimientoInventario movimiento = movs.get(0);

        BigDecimal yaReversado = safeGetYaReversado(movimiento.getId());
        BigDecimal disponible = movimiento.getCantidad().subtract(yaReversado);

        if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MinorExcepcion("YA_REVERSADO", "Este ítem ya fue reversado completamente");
        }
        if (req.getCantidadReversada().compareTo(disponible) > 0) {
            throw new MinorExcepcion("CANTIDAD_EXCEDE",
                    "La cantidad a reversar (" + req.getCantidadReversada().toPlainString()
                            + ") supera la disponible (" + disponible.toPlainString() + ")");
        }

        return reversarMovimientoIndividual(movimiento, req.getCantidadReversada(),
                req.getObservacion(), req.getUsuarioId(), req.getCajaId());
    }

    // -----------------------------------------------------------------------
    // Reverso combo para CAJA/DEVOLUCIONES (cantidadReversada = unidades de combo)
    // -----------------------------------------------------------------------

    private ReversoResponse crearReversoVentaDetalleCombo(VentaDetalle vd, VentaDetalleReversoRequest req) {

        List<MovimientoInventario> movimientos = movimientoService.buscarTodosPorReferenciaYTipo(
                MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE, vd.getId());

        if (movimientos.isEmpty()) {
            throw new MinorExcepcion("COMBO_SIN_MOVIMIENTOS",
                    "No se encontraron movimientos de inventario para el combo (ventaDetalleId=" + vd.getId() + ")");
        }

        BigDecimal disponibleCombos = calcularDisponibleCombos(vd, movimientos);

        if (disponibleCombos.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MinorExcepcion("COMBO_YA_REVERSADO", "El combo ya fue reversado completamente");
        }
        if (req.getCantidadReversada().compareTo(disponibleCombos) > 0) {
            throw new MinorExcepcion("CANTIDAD_EXCEDE",
                    "La cantidad a reversar (" + req.getCantidadReversada().toPlainString()
                            + ") supera los combos disponibles (" + disponibleCombos.toPlainString() + ")");
        }

        Reverso ultimoReverso = null;
        for (MovimientoInventario mov : movimientos) {
            // qty por movimiento = (mov.cantidad / vd.cantidad) * combosAReverse
            BigDecimal cantidadAReversar = mov.getCantidad()
                    .divide(vd.getCantidad(), 10, RoundingMode.HALF_UP)
                    .multiply(req.getCantidadReversada())
                    .setScale(2, RoundingMode.HALF_UP);

            Reverso reverso = buildReverso(mov.getId(), cantidadAReversar, req.getObservacion(), req.getUsuarioId());
            ultimoReverso = reversoRepository.save(reverso);

            MovimientoInventario movReverso = buildMovimientoReverso(mov, ultimoReverso.getId(), cantidadAReversar, req.getUsuarioId());
            movimientoService.guardarMovimiento(movReverso);
            stockActualService.actualizarStock(movReverso);

            log.info("crearReversoVentaDetalleCombo: reversoId={} movimientoId={} cantidad={}",
                    ultimoReverso.getId(), mov.getId(), cantidadAReversar);
        }

        // Un solo egreso de caja por el total del combo
        BigDecimal montoDevolucion = vd.getPrecioUnitario().multiply(req.getCantidadReversada());
        registrarMovimientoCaja(ultimoReverso.getId(), montoDevolucion, req.getUsuarioId(), req.getCajaId());

        return reversoMapper.toResponse(ultimoReverso);
    }

    // -----------------------------------------------------------------------
    // Reverso combo completo — flujo MOVIMIENTOS (reversa todo lo disponible)
    // -----------------------------------------------------------------------

    private ReversoResponse reversarComboCompleto(VentaDetalle vd, Long usuarioId, Long cajaId, String observacion) {

        List<MovimientoInventario> movimientos = movimientoService.buscarTodosPorReferenciaYTipo(
                MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE, vd.getId());

        if (movimientos.isEmpty()) {
            throw new MinorExcepcion("COMBO_SIN_MOVIMIENTOS",
                    "No se encontraron movimientos de inventario para el combo (ventaDetalleId=" + vd.getId() + ")");
        }

        BigDecimal disponibleCombos = calcularDisponibleCombos(vd, movimientos);

        if (disponibleCombos.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MinorExcepcion("COMBO_YA_REVERSADO", "El combo ya fue reversado completamente");
        }

        Reverso ultimoReverso = null;
        for (MovimientoInventario mov : movimientos) {
            BigDecimal yaReversado = safeGetYaReversado(mov.getId());
            BigDecimal disponible = mov.getCantidad().subtract(yaReversado);

            Reverso reverso = buildReverso(mov.getId(), disponible, observacion, usuarioId);
            ultimoReverso = reversoRepository.save(reverso);

            MovimientoInventario movReverso = buildMovimientoReverso(mov, ultimoReverso.getId(), disponible, usuarioId);
            movimientoService.guardarMovimiento(movReverso);
            stockActualService.actualizarStock(movReverso);

            log.info("reversarComboCompleto: reversoId={} movimientoId={} cantidad={}",
                    ultimoReverso.getId(), mov.getId(), disponible);
        }

        // Un solo egreso de caja por los combos reversados
        BigDecimal montoDevolucion = vd.getPrecioUnitario().multiply(disponibleCombos);
        if (cajaId != null) {
            registrarMovimientoCaja(ultimoReverso.getId(), montoDevolucion, usuarioId, cajaId);
        }

        return reversoMapper.toResponse(ultimoReverso);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ReversoResponse reversarMovimientoIndividual(MovimientoInventario movimiento,
            BigDecimal cantidad, String observacion, Long usuarioId, Long cajaId) {

        Reverso reverso = buildReverso(movimiento.getId(), cantidad, observacion, usuarioId);
        Reverso reversoGuardado = reversoRepository.save(reverso);

        log.info("reversarMovimientoIndividual: reversoId={} movimientoId={} cantidad={}",
                reversoGuardado.getId(), movimiento.getId(), cantidad);

        MovimientoInventario movReverso = buildMovimientoReverso(movimiento, reversoGuardado.getId(), cantidad, usuarioId);
        movimientoService.guardarMovimiento(movReverso);
        stockActualService.actualizarStock(movReverso);

        boolean esReversoDeVenta = MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE
                .equalsIgnoreCase(movimiento.getReferenciaTipo());

        if (esReversoDeVenta) {
            VentaDetalle vd = ventaDetalleService.buscarPorId(movimiento.getReferenciaId())
                    .orElseThrow(() -> new MinorExcepcion("VD_NO_ENCONTRADO",
                            "No existe el detalle de venta referenciaId=" + movimiento.getReferenciaId()));
            BigDecimal monto = cantidad.multiply(vd.getPrecioUnitario());
            registrarMovimientoCaja(reversoGuardado.getId(), monto, usuarioId, cajaId);
        }

        return reversoMapper.toResponse(reversoGuardado);
    }

    private Reverso buildReverso(Long movimientoId, BigDecimal cantidad, String observacion, Long usuarioId) {
        Reverso r = new Reverso();
        r.setMovimientoId(movimientoId);
        r.setCantidadReversada(cantidad);
        r.setObservacion(observacion);
        r.setUsuarioId(usuarioId);
        r.setFechaCreacion(LocalDateTime.now());
        return r;
    }

    private MovimientoInventario buildMovimientoReverso(MovimientoInventario original, Long reversoId,
            BigDecimal cantidad, Long usuarioId) {
        String tipoReverso = MovimientoInventarioConstants.TIPO_SALIDA.equalsIgnoreCase(original.getTipo())
                ? MovimientoInventarioConstants.TIPO_ENTRADA
                : MovimientoInventarioConstants.TIPO_SALIDA;

        MovimientoInventario m = new MovimientoInventario();
        m.setProductoId(original.getProductoId());
        m.setTipo(tipoReverso);
        m.setCantidad(cantidad);
        m.setCostoUnitario(original.getCostoUnitario());
        m.setCostoTotal(cantidad.multiply(original.getCostoUnitario()));
        m.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_REVERSO);
        m.setReferenciaId(reversoId);
        m.setMovimientoOrigenId(original.getId());
        m.setFechaCreacion(LocalDateTime.now());
        m.setUsuarioId(usuarioId);
        return m;
    }

    private void registrarMovimientoCaja(Long reversoId, BigDecimal monto, Long usuarioId, Long cajaId) {
        MovimientoCaja movCaja = new MovimientoCaja();
        movCaja.setCajaId(cajaId);
        movCaja.setTipo(CajaConstants.TIPO_EGRESO);
        movCaja.setMetodoPago(CajaConstants.METODO_PAGO_EFECTIVO);
        movCaja.setMonto(monto);
        movCaja.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_REVERSO);
        movCaja.setReferenciaId(reversoId);
        movCaja.setFechaCreacion(LocalDateTime.now());
        movCaja.setUsuarioId(usuarioId);
        movimientoService.guardarMovimientoCaja(movCaja);
        log.info("registrarMovimientoCaja: cajaId={} monto={}", cajaId, monto);
    }

    private BigDecimal calcularDisponibleCombos(VentaDetalle vd, List<MovimientoInventario> movimientos) {
        BigDecimal minimo = null;
        for (MovimientoInventario mov : movimientos) {
            BigDecimal yaReversado = safeGetYaReversado(mov.getId());
            BigDecimal disponible = mov.getCantidad().subtract(yaReversado);
            // combosDisponibles = disponible * vd.cantidad / mov.cantidad
            BigDecimal combosDisponibles = disponible
                    .multiply(vd.getCantidad())
                    .divide(mov.getCantidad(), 0, RoundingMode.FLOOR);
            if (minimo == null || combosDisponibles.compareTo(minimo) < 0) {
                minimo = combosDisponibles;
            }
        }
        return minimo != null ? minimo : BigDecimal.ZERO;
    }

    private BigDecimal safeGetYaReversado(Long movimientoId) {
        BigDecimal total = reversoRepository.sumCantidadReversadaByMovimientoId(movimientoId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // -----------------------------------------------------------------------
    // Métodos existentes sin cambios
    // -----------------------------------------------------------------------

    @Override
    public BigDecimal getTotalReversosHoy() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        BigDecimal total = movimientoCajaRepository.sumMontoByReferenciaTipoAndFechaCreacionBetween(
                MovimientoInventarioConstants.REFERENCIA_REVERSO, inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getCantidadReversada(Long movimientoId) {
        BigDecimal total = reversoRepository.sumCantidadReversadaByMovimientoId(movimientoId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public ReversoResponse obtenerPorId(Long id) {
        return reversoRepository.findById(id)
            .map(reversoMapper::toResponse)
            .orElseThrow(() -> new MinorExcepcion("NOT_FOUND", "Reverso no encontrado con id=" + id));
    }
}
