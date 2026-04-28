package co.com.menor.commerce_core_bd.inventario.service;

import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioDetalladoResponse;
import org.springframework.data.domain.Page;

public interface InventarioService {

    MovimientoInventario registrarMovimiento(CreateMovimientoInventarioRequest req);

    void actualizarStock(MovimientoInventario movimiento);

    Page<MovimientoInventarioDetalladoResponse> obtenerMovimientosPaginados(FiltroMovimientoInventarioRequest filtro);
}
