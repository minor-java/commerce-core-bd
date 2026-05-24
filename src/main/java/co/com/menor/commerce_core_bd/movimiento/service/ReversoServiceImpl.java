package co.com.menor.commerce_core_bd.movimiento.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.movimiento.mapper.ReversoMapper;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.Reverso;
import co.com.menor.commerce_core_bd.movimiento.repository.ReversoRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.service.VentaDetalleService;
import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;
import co.com.menor.comun_dto.utils.CajaConstants;
import co.com.menor.comun_dto.utils.MovimientoInventarioConstants;
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

    private final ReversoRepository reversoRepository;

    private final ReversoMapper reversoMapper;

    private final StockActualService stockActualService;
    private final VentaDetalleService ventaDetalleService;
    private final MovimientoService movimientoService;

    @Override
    @Transactional
    public ReversoResponse crearReverso(ReversoRequest req) {

        try {

            MovimientoInventario movimiento = movimientoService.buscarPorId(req.getMovimientoId()).get();

            Reverso reverso = reversoMapper.toEntity(req);
            Reverso reversoGuardado = reversoRepository.save(reverso);

            log.info(
                "crearReverso: reverso id={} movimientoId={} cantidad={}",
                reversoGuardado.getId(), movimiento.getId(), req.getCantidadReversada()
            );

            String tipoReverso = MovimientoInventarioConstants.TIPO_SALIDA.equalsIgnoreCase(movimiento.getTipo())
                    ? MovimientoInventarioConstants.TIPO_ENTRADA
                    : MovimientoInventarioConstants.TIPO_SALIDA;
            MovimientoInventario movReverso = new MovimientoInventario();

            movReverso.setProductoId(movimiento.getProductoId());
            movReverso.setTipo(tipoReverso);
            movReverso.setCantidad(req.getCantidadReversada());
            movReverso.setCostoUnitario(movimiento.getCostoUnitario());
            movReverso.setCostoTotal(req.getCantidadReversada().multiply(movimiento.getCostoUnitario()));
            movReverso.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_REVERSO);
            movReverso.setReferenciaId(reversoGuardado.getId());
            movReverso.setMovimientoOrigenId(movimiento.getId());
            movReverso.setFechaCreacion(LocalDateTime.now());
            movReverso.setUsuarioId(req.getUsuarioId());

            movimientoService.guardarMovimiento(movReverso);

            stockActualService.actualizarStock(movReverso);
            boolean esReversoDeVenta = MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE.equalsIgnoreCase(movimiento.getReferenciaTipo());

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

    private void registrarMovimientoCajaReversoVenta(
        Long reversoId,
        Long referenciaId,
        Long usuarioId,
        BigDecimal cantidadReversada,
        Long cajaId
    ) {

        VentaDetalle ventaDetalle = ventaDetalleService.buscarPorId(referenciaId).get();
        BigDecimal montoDevolucion = cantidadReversada.multiply(ventaDetalle.getPrecioUnitario());

        MovimientoCaja movCaja = new MovimientoCaja();
        movCaja.setCajaId(cajaId);
        movCaja.setTipo(CajaConstants.TIPO_EGRESO);
        movCaja.setMetodoPago(CajaConstants.METODO_PAGO_EFECTIVO);
        movCaja.setMonto(montoDevolucion);
        movCaja.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_REVERSO);
        movCaja.setReferenciaId(reversoId);
        movCaja.setFechaCreacion(LocalDateTime.now());
        movCaja.setUsuarioId(usuarioId);

        movimientoService.guardarMovimientoCaja(movCaja);

        log.info("registrarMovimientoCajaReversoVenta: cajaId={} monto={}", cajaId, montoDevolucion);
    }
}
