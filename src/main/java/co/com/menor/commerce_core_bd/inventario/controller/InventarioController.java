package co.com.menor.commerce_core_bd.inventario.controller;

import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioDetalladoResponse;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import co.com.menor.commerce_core_bd.inventario.mapper.MovimientoInventarioResponseMapper;
import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
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

    private final InventarioService inventarioService;
    private final MovimientoInventarioResponseMapper responseMapper;

    @PostMapping("/guardar")
    public ResponseEntity<MovimientoInventarioResponse> guardar(
            @RequestBody CreateMovimientoInventarioRequest request) {
        MovimientoInventario saved = inventarioService.registrarMovimiento(request);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<PaginadoResponse<MovimientoInventarioDetalladoResponse>> obtenerMovimientos(
            @RequestBody FiltroMovimientoInventarioRequest filtro) {
        Page<MovimientoInventarioDetalladoResponse> page = inventarioService.obtenerMovimientosPaginados(filtro);
        PaginadoResponse<MovimientoInventarioDetalladoResponse> respuesta = new PaginadoResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(respuesta);
    }
}
