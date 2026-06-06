package co.com.menor.commerce_core_bd.movimiento.service;

import java.math.BigDecimal;

import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.request.VentaDetalleReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;

public interface ReversoService {

    ReversoResponse crearReverso(ReversoRequest req);

    ReversoResponse crearReversoVentaDetalle(VentaDetalleReversoRequest req);

    BigDecimal getCantidadReversada(Long movimientoId);

    ReversoResponse obtenerPorId(Long id);

    BigDecimal getTotalReversosHoy();
}
