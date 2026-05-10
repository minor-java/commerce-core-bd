package co.com.menor.commerce_core_bd.movimiento.service;

import java.util.Optional;

import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.StockActual;

public interface StockActualService {

    void actualizarStock(MovimientoInventario movimiento);
    Optional<StockActual> buscarPorProductoId(Long productoId);
}
