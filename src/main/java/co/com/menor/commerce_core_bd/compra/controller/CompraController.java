package co.com.menor.commerce_core_bd.compra.controller;

import co.com.menor.commerce_core_bd.compra.service.CompraService;
import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleByIdResponse;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    public ResponseEntity<CompraResponse> crear(@RequestBody CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.crearCompra(request));
    }

    @GetMapping
    public ResponseEntity<List<CompraResponse>> obtenerTodas() {
        return ResponseEntity.ok(compraService.obtenerTodas());
    }

    @PostMapping("/detalladas")
    public ResponseEntity<PaginadoResponse<CompraResponse>> buscarDetalladas(
            @RequestBody FiltroCompraRequest filtro) {
        Page<CompraResponse> page = compraService.buscarDetalladas(filtro);
        PaginadoResponse<CompraResponse> respuesta = new PaginadoResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerPorId(id));
    }

    @GetMapping("/detalle-by-id/{id}")
    public ResponseEntity<CompraDetalleByIdResponse> obtenerDetallePorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerDetallePorId(id));
    }

}
