package co.com.menor.commerce_core_bd.venta.service;

import java.util.List;
import java.util.Optional;

import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;

public interface VentaDetalleService {

    List<VentaDetalle> guardarTodo(List<VentaDetalle> detalles);

    List<VentaDetalle> buscarPorVentaId(Long id);

    Optional<VentaDetalle> buscarPorId(Long id);

    List<VentaDetalle> buscarPorVentaIds(List<Long> ventaIds);
}
