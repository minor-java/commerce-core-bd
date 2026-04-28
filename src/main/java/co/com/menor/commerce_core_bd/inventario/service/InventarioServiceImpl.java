package co.com.menor.commerce_core_bd.inventario.service;

import co.com.menor.commerce_core_bd.compra.mapper.CompraResponseMapper;
import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.commerce_core_bd.compra.repository.CompraDetalleRepository;
import co.com.menor.commerce_core_bd.compra.repository.CompraRepository;
import co.com.menor.comun_dto.compra.response.CompraDetalleByIdResponse;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioDetalladoResponse;
import co.com.menor.commerce_core_bd.inventario.mapper.MovimientoInventarioMapper;
import co.com.menor.commerce_core_bd.inventario.mapper.MovimientoInventarioResponseMapper;
import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.inventario.model.StockActual;
import co.com.menor.commerce_core_bd.inventario.repository.MovimientoInventarioRepository;
import co.com.menor.commerce_core_bd.inventario.repository.MovimientoInventarioSpecification;
import co.com.menor.commerce_core_bd.inventario.repository.StockActualRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private static final String TIPO_ENTRADA = "ENTRADA";
    private static final String TIPO_SALIDA = "SALIDA";
    private static final String REFERENCIA_COMPRA_DETALLE = "COMPRA_DETALLE";

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockActualRepository stockActualRepository;
    private final MovimientoInventarioMapper mapper;
    private final MovimientoInventarioResponseMapper responseMapper;
    private final CompraDetalleRepository compraDetalleRepository;
    private final CompraRepository compraRepository;
    private final CompraResponseMapper compraResponseMapper;

    @Override
    @Transactional
    public MovimientoInventario registrarMovimiento(CreateMovimientoInventarioRequest req) {
        if (req == null) {
            throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        }
        if (req.getProductoId() == null || req.getReferenciaId() == null || req.getCreadoPor() == null) {
            throw new MinorExcepcion("CAMPOS_OBLIGATORIOS_FALTANTES",
                    "Los campos productoId, referenciaId y creadoPor son obligatorios");
        }
        if (req.getCantidad() == null || req.getCostoUnitario() == null || req.getTipo() == null) {
            throw new MinorExcepcion("CAMPOS_OBLIGATORIOS_FALTANTES",
                    "Los campos cantidad, costoUnitario y tipo son obligatorios");
        }

        MovimientoInventario movimiento = mapper.toEntity(req);
        movimiento.setCostoTotal(req.getCantidad().multiply(req.getCostoUnitario()));
        movimiento.setFechaCreacion(LocalDateTime.now());

        MovimientoInventario saved = movimientoRepository.save(movimiento);
        log.info("registrarMovimiento: id={} productoId={} tipo={} referenciaTipo={}",
                saved.getId(), saved.getProductoId(), saved.getTipo(), saved.getReferenciaTipo());

        actualizarStock(saved);
        return saved;
    }

    @Override
    public void actualizarStock(MovimientoInventario movimiento) {
        Optional<StockActual> stockOpt = stockActualRepository.findByProductoId(movimiento.getProductoId());

        StockActual stock;
        if (stockOpt.isPresent()) {
            stock = stockOpt.get();
            if (TIPO_SALIDA.equalsIgnoreCase(movimiento.getTipo())) {
                stock.setStock(stock.getStock().subtract(movimiento.getCantidad()));
            } else {
                BigDecimal nuevoStock = stock.getStock().add(movimiento.getCantidad());
                // Solo recalcular costo promedio en entradas por compra
                if (REFERENCIA_COMPRA_DETALLE.equalsIgnoreCase(movimiento.getReferenciaTipo())) {
                    BigDecimal costoActual = stock.getStock().multiply(stock.getCostoPromedio());
                    BigDecimal costoNuevo = movimiento.getCantidad().multiply(movimiento.getCostoUnitario());
                    BigDecimal nuevoCostoPromedio = costoActual.add(costoNuevo)
                            .divide(nuevoStock, 4, RoundingMode.HALF_UP);
                    stock.setCostoPromedio(nuevoCostoPromedio);
                }
                stock.setStock(nuevoStock);
            }
        } else {
            stock = new StockActual();
            stock.setProductoId(movimiento.getProductoId());
            BigDecimal stockInicial = TIPO_SALIDA.equalsIgnoreCase(movimiento.getTipo())
                    ? movimiento.getCantidad().negate()
                    : movimiento.getCantidad();
            stock.setStock(stockInicial);
            stock.setCostoPromedio(movimiento.getCostoUnitario());
        }

        stock.setFechaActualizacion(LocalDateTime.now());
        stockActualRepository.save(stock);
        log.info("actualizarStock: productoId={} stock={}", stock.getProductoId(), stock.getStock());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDetalladoResponse> obtenerMovimientosPaginados(
            FiltroMovimientoInventarioRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Page<MovimientoInventario> page = movimientoRepository.findAll(
                MovimientoInventarioSpecification.buildFrom(filtro), pageable);
        return page.map(this::enriquecerConDetalle);
    }

    private MovimientoInventarioDetalladoResponse enriquecerConDetalle(MovimientoInventario movimiento) {
        CompraDetalleByIdResponse detalleCompra = null;
        if (REFERENCIA_COMPRA_DETALLE.equalsIgnoreCase(movimiento.getReferenciaTipo())
                && movimiento.getReferenciaId() != null) {
            try {
                CompraDetalle detalle = compraDetalleRepository.findById(movimiento.getReferenciaId())
                        .orElse(null);
                if (detalle != null) {
                    Compra compra = compraRepository.findById(detalle.getCompraId()).orElse(null);
                    if (compra != null) {
                        detalleCompra = compraResponseMapper.toDetalleByIdResponse(detalle, compra);
                    }
                }
            } catch (Exception e) {
                log.warn("enriquecerConDetalle: no se pudo obtener detalle compra para referenciaId={} movimientoId={}",
                        movimiento.getReferenciaId(), movimiento.getId(), e);
            }
        }
        return responseMapper.toDetalladoResponse(movimiento, detalleCompra);
    }
}
