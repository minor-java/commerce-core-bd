package co.com.menor.commerce_core_bd.movimiento.controller;

import co.com.menor.commerce_core_bd.movimiento.mapper.MovimientoInventarioMapper;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.service.MovimientoService;
import co.com.menor.comun_dto.caja.request.SumaMovimientoCajaRequest;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final MovimientoService inventarioService;
    private final MovimientoInventarioMapper responseMapper;

    @PostMapping("/guardar")
    public ResponseEntity<MovimientoInventarioResponse> guardar(
            @RequestBody CreateMovimientoInventarioRequest request
    ) {
        return ResponseEntity.ok(
            responseMapper.toResponse(
                inventarioService.registrarMovimiento(request)
            )
        );
    }

    @PostMapping("/movimientos")
    public ResponseEntity<PaginadoResponse<MovimientoInventarioResponse>> obtenerMovimientos(
        @RequestBody FiltroMovimientoInventarioRequest filtro
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
}
