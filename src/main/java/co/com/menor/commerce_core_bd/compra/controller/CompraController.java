package co.com.menor.commerce_core_bd.compra.controller;

import co.com.menor.commerce_core_bd.compra.service.CompraService;
import co.com.menor.commerce_core_bd.compra.service.CompreDetalleService;
import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compra")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;
    private final CompreDetalleService compreDetalleService;

    @PostMapping
    public ResponseEntity<CompraResponse> crear(@RequestBody CompraRequest request) {
        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(compraService.crearCompra(request));
    }

    @PostMapping("/paginado")
    public ResponseEntity<PaginadoResponse<CompraResponse>> buscarDetalladas(
        @RequestBody FiltroCompraRequest filtro
    ) {

        Page<CompraResponse> page = compraService.buscarComprasPaginado(filtro);

        PaginadoResponse<CompraResponse> respuesta = new PaginadoResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages()
        );

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/consulta-detalle-por-compra-id/{id}")
    public ResponseEntity<List<CompraDetalleResponse>> buscarPorCompraId(
        @PathVariable Long id
    ) {

        return ResponseEntity.ok(
            compreDetalleService.obtenerDetallesPorCompraId(id)
        );
    }
}
