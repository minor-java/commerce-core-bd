package co.com.menor.commerce_core_bd.movimiento.controller;

import co.com.menor.commerce_core_bd.movimiento.service.MovimientoService;
import co.com.menor.commerce_core_bd.movimiento.service.StockActualService;
import co.com.menor.comun_dto.caja.request.SumaMovimientoCajaRequest;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroStockRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;
import co.com.menor.comun_dto.inventario.response.StockActualResponse;
import co.com.menor.comun_dto.inventario.response.StockPaginadoResponse;
import co.com.menor.comun_dto.inventario.response.StockResumenResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final MovimientoService inventarioService;    
    private final StockActualService stockActualService;    

    @GetMapping("/movimiento/{id}")
    public ResponseEntity<MovimientoInventarioResponse> getMovimientoById(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.getMovimientoById(id));
    }

    @GetMapping("/movimiento/por-referencia/{tipo}/{referenciaId}")
    public ResponseEntity<MovimientoInventarioResponse> buscarPorReferencia(
            @PathVariable String tipo,
            @PathVariable Long referenciaId) {
        return ResponseEntity.ok(inventarioService.buscarPorReferenciaYTipo(tipo, referenciaId));
    }

    @PostMapping("/guardar")
    public ResponseEntity<MovimientoInventarioResponse> guardar(
            @RequestBody CreateMovimientoInventarioRequest request
    ) {
        return ResponseEntity.ok(
            inventarioService.registrarMovimiento(request) 
        );
    }

    @PostMapping("/movimientos")
    public ResponseEntity<PaginadoResponse<MovimientoInventarioResponse>> obtenerMovimientos(
        @RequestBody(required = false) FiltroMovimientoInventarioRequest filtro
    ) {

        Page<MovimientoInventarioResponse> page = 
        inventarioService.obtenerMovimientosPaginados(filtro);
        
        PaginadoResponse<MovimientoInventarioResponse> respuesta = new PaginadoResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages()
        );

        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/suma/movimientos")
    public ResponseEntity<BigDecimal> sumaMovimientosCaja(
        @RequestBody SumaMovimientoCajaRequest request
    ) {
        return ResponseEntity.ok(inventarioService.sumaMovimientosCaja(request));
    }

    @PostMapping("/suma/movimientos-por-tipo")
    public ResponseEntity<BigDecimal> sumaMovimientosCajaTipo(
        @RequestBody SumaMovimientoCajaRequest request
    ) {
        return ResponseEntity.ok(inventarioService.sumaMovimientosCajaTipo(request));
    }

    @GetMapping("/stock/{productoId}")
    public ResponseEntity<StockActualResponse> consultarStock(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.consultarStock(productoId));
    }

    @GetMapping("/stock/consulta-por-producto-id/{productoId}")
    public ResponseEntity<StockActualResponse> consultarStockPorProductoId(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.consultarStock(productoId));
    }

    @PostMapping("/stock/paginado")
    public ResponseEntity<PaginadoResponse<StockPaginadoResponse>> obtenerStockPaginado(
        @RequestBody(required = false) FiltroStockRequest filtro
    ) {
        Page<StockPaginadoResponse> page = stockActualService.obtenerStockPaginado(filtro);

        PaginadoResponse<StockPaginadoResponse> respuesta = new PaginadoResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages()
        );

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/stock/resumen")
    public ResponseEntity<StockResumenResponse> obtenerResumenStock() {
        return ResponseEntity.ok(stockActualService.obtenerResumen());
    }
}
