package co.com.menor.commerce_core_bd.movimiento.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.movimiento.mapper.MovimientoInventarioMapper;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.repository.MovimientoCajaRepository;
import co.com.menor.commerce_core_bd.movimiento.repository.MovimientoInventarioRepository;
import co.com.menor.commerce_core_bd.movimiento.repository.MovimientoInventarioSpecification;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.caja.request.SumaMovimientoCajaRequest;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;
import co.com.menor.comun_dto.inventario.response.StockActualResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    
    private final StockActualService stockActualService;

    private final MovimientoInventarioMapper movimientoInventarioMapper;

    @Override
    public MovimientoInventario guardarMovimiento(MovimientoInventario movimiento) {

        return movimientoRepository.save(movimiento);
    }

    @Override
    public Optional<MovimientoInventario> buscarPorId(Long movimientoId) {

        return movimientoRepository.findById(movimientoId);
    }

    @Override
    public MovimientoInventarioResponse getMovimientoById(Long id) {

        return movimientoRepository.findById(id)
            .map(movimientoInventarioMapper::toResponse)
            .orElseThrow(() -> new MinorExcepcion(
                "MOVIMIENTO_NO_ENCONTRADO",
                "No existe un movimiento con id " + id
            ));
    }

    @Override
    @Transactional
    public MovimientoInventarioResponse registrarMovimiento(CreateMovimientoInventarioRequest req) {

        try {
            
            MovimientoInventario movimiento = movimientoInventarioMapper.toEntity(req);
            movimiento.setCostoTotal(req.getCantidad().multiply(req.getCostoUnitario()));
            movimiento.setFechaCreacion(LocalDateTime.now());
    
            MovimientoInventario saved = guardarMovimiento(movimiento);
    
            log.info(
                "registrarMovimiento: id={} productoId={} tipo={} referenciaTipo={}",
                saved.getId(), saved.getProductoId(), saved.getTipo(), saved.getReferenciaTipo()
            );
    
            stockActualService.actualizarStock(saved);
            return movimientoInventarioMapper.toResponse(saved);

        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "MovimientoService registrarMovimiento"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioResponse> obtenerMovimientosPaginados(
        FiltroMovimientoInventarioRequest filtro
    ) {

        try {

            if (filtro == null) {
                filtro = new FiltroMovimientoInventarioRequest();
            }

            PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());

            Page<MovimientoInventario> page = movimientoRepository.findAll(
                MovimientoInventarioSpecification.buildFrom(filtro),
                pageable
            );
    
            return page.map(movimientoInventarioMapper::toResponse);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "MovimientoService obtenerMovimientosPaginados"
            );
        }
    }

    @Override
    public BigDecimal sumaMovimientosCaja(SumaMovimientoCajaRequest req) {

        try {
            
            return movimientoCajaRepository
            .sumMontoByCajaIdAndTipoAndMetodoPago(
                req.getCajaId(),
                req.getTipo(),
                req.getMetodoPago()
            );
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "MovimientoService sumaMovimientosCaja"
            );
        }
    }

    @Override
    public BigDecimal sumaMovimientosCajaTipo(SumaMovimientoCajaRequest req) {

        try {
            
            return movimientoCajaRepository
            .sumMontoByCajaIdAndTipo(
                req.getCajaId(), 
                req.getTipo()
            );
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "MovimientoService sumaMovimientosCajaTipo"
            );
        }
    }

    @Override
    public MovimientoCaja guardarMovimientoCaja(MovimientoCaja movCaja) {

        try {

            return movimientoCajaRepository
            .save(movCaja);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "MovimientoService guardarmovimientoCaja"
            );
        }
    }

    @Override
    public StockActualResponse consultarStock(Long productoId) {

        return stockActualService.buscarPorProductoId(productoId)
            .map(s -> new StockActualResponse(
                s.getProductoId(),
                s.getStock(),
                s.getCostoPromedio(),
                s.getFechaActualizacion()
            ))
            .orElse(new StockActualResponse(productoId, BigDecimal.ZERO, BigDecimal.ZERO, null));
    }
}
