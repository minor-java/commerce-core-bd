package co.com.menor.commerce_core_bd.compra.service;

import java.util.List;

import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;

public interface CompreDetalleService {

    CompraDetalleResponse obtenerCompraDetallePorId(Long detalleId);

    List<CompraDetalle> guardarDetalles(List<CompraDetalle> detalles);

    List<CompraDetalleResponse> obtenerDetallesPorCompraId(Long compraId);
}
