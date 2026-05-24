package co.com.menor.commerce_core_bd.movimiento.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.comun_dto.caja.request.SumaMovimientoCajaRequest;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroStockRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;
import co.com.menor.comun_dto.inventario.response.StockActualResponse;
import co.com.menor.comun_dto.inventario.response.StockPaginadoResponse;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface MovimientoService {

    MovimientoInventarioResponse registrarMovimiento(CreateMovimientoInventarioRequest req);

    StockActualResponse consultarStock(Long productoId);

    Page<MovimientoInventarioResponse> obtenerMovimientosPaginados(
        FiltroMovimientoInventarioRequest filtro
    );

    MovimientoInventario guardarMovimiento(MovimientoInventario movimiento);

    Optional<MovimientoInventario> buscarPorId(Long movimientoId);

    MovimientoInventarioResponse getMovimientoById(Long id);

    BigDecimal sumaMovimientosCaja(SumaMovimientoCajaRequest req);

    BigDecimal sumaMovimientosCajaTipo(SumaMovimientoCajaRequest req);

    MovimientoCaja guardarMovimientoCaja(MovimientoCaja movCaja);

    MovimientoInventarioResponse buscarPorReferenciaYTipo(String referenciaTipo, Long referenciaId);
}
