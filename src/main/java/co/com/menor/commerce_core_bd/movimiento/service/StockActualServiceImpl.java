package co.com.menor.commerce_core_bd.movimiento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.model.StockActual;
import co.com.menor.commerce_core_bd.movimiento.repository.StockActualRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.inventario.request.FiltroStockRequest;
import co.com.menor.comun_dto.inventario.response.StockPaginadoResponse;
import co.com.menor.comun_dto.utils.MovimientoInventarioConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockActualServiceImpl implements StockActualService {

    private final StockActualRepository stockActualRepository;

    @Override
    public void actualizarStock(MovimientoInventario movimiento) {

        try {

            Optional<StockActual> stockOpt = stockActualRepository.findByProductoId(
                movimiento.getProductoId()
            );

            StockActual stock;
            if (stockOpt.isPresent()) {

                stock = stockOpt.get();

                if (MovimientoInventarioConstants.TIPO_SALIDA.equalsIgnoreCase(movimiento.getTipo())) {

                    stock.setStock(stock.getStock().subtract(movimiento.getCantidad()));
                } else {

                    BigDecimal nuevoStock = stock.getStock().add(movimiento.getCantidad());
                    // Solo recalcular costo promedio en entradas por compra
                    if (MovimientoInventarioConstants.REFERENCIA_COMPRA_DETALLE.equalsIgnoreCase(movimiento.getReferenciaTipo())) {

                        BigDecimal costoActual = stock
                            .getStock()
                            .multiply(stock.getCostoPromedio());

                        BigDecimal costoNuevo = movimiento
                            .getCantidad()
                            .multiply(movimiento.getCostoUnitario());

                        BigDecimal nuevoCostoPromedio = costoActual
                            .add(costoNuevo)
                            .divide(nuevoStock, 4, RoundingMode.HALF_UP);

                        stock.setCostoPromedio(nuevoCostoPromedio);
                    }

                    stock.setStock(nuevoStock);
                }

            } else {

                stock = new StockActual();
                stock.setProductoId(movimiento.getProductoId());

                BigDecimal stockInicial = MovimientoInventarioConstants.TIPO_SALIDA.equalsIgnoreCase(movimiento.getTipo())
                        ? movimiento.getCantidad().negate()
                        : movimiento.getCantidad();

                stock.setStock(stockInicial);
                stock.setCostoPromedio(movimiento.getCostoUnitario());
            }

            stock.setFechaActualizacion(LocalDateTime.now());
            stockActualRepository.save(stock);
            log.info("actualizarStock: productoId={} stock={}", stock.getProductoId(), stock.getStock());

        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "StockActualService actualizarStock"
            );
        }
    }

    @Override
    public Optional<StockActual> buscarPorProductoId(Long productoId) {

        try {

            return stockActualRepository.findByProductoId(productoId);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "StockActualService buscarPorProductoId"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockPaginadoResponse> obtenerStockPaginado(FiltroStockRequest filtro) {

        try {

            if (filtro == null) {
                filtro = new FiltroStockRequest();
            }

            PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());

            return stockActualRepository.findStockPaginado(
                filtro.getNombre(),
                filtro.getPresentacionValor(),
                filtro.getPresentacionUnidad(),
                filtro.getActivo(),
                filtro.getPrecioVenta(),
                pageable
            );

        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "StockActualService obtenerStockPaginado"
            );
        }
    }
}
