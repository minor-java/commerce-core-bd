package co.com.menor.commerce_core_bd.movimiento.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.StockActual;
import co.com.menor.comun_dto.inventario.request.FiltroStockRequest;
import co.com.menor.comun_dto.inventario.response.StockPaginadoResponse;

public interface StockActualService {

    void actualizarStock(MovimientoInventario movimiento);
    Optional<StockActual> buscarPorProductoId(Long productoId);
    Page<StockPaginadoResponse> obtenerStockPaginado(FiltroStockRequest filtro);
}
