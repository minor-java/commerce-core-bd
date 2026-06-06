package co.com.menor.commerce_core_bd.combo.controller;

import co.com.menor.commerce_core_bd.combo.dto.CostoComboRequest;
import co.com.menor.commerce_core_bd.combo.dto.CostoComboResponse;
import co.com.menor.commerce_core_bd.combo.service.ComboService;
import co.com.menor.comun_dto.combo.request.CreateComboRequest;
import co.com.menor.comun_dto.combo.request.FiltroComboRequest;
import co.com.menor.comun_dto.combo.request.UpdateComboRequest;
import co.com.menor.comun_dto.combo.response.ComboResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/combo")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @PostMapping
    public ResponseEntity<ComboResponse> crear(@RequestBody CreateComboRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comboService.crearCombo(request));
    }

    @PostMapping("/paginado")
    public ResponseEntity<PaginadoResponse<ComboResponse>> buscarPaginado(@RequestBody FiltroComboRequest filtro) {
        Page<ComboResponse> page = comboService.buscarPaginado(filtro);
        return ResponseEntity.ok(new PaginadoResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComboResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comboService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComboResponse> actualizar(@PathVariable Long id, @RequestBody UpdateComboRequest request) {
        return ResponseEntity.ok(comboService.actualizarCombo(id, request));
    }

    @GetMapping("/stock-comprometido/{productoId}")
    public ResponseEntity<BigDecimal> getStockComprometido(@PathVariable Long productoId) {
        return ResponseEntity.ok(comboService.getStockComprometido(productoId));
    }

    @PostMapping("/stock-comprometido/batch")
    public ResponseEntity<Map<Long, BigDecimal>> getStockComprometidoBatch(@RequestBody List<Long> productoIds) {
        return ResponseEntity.ok(comboService.getStockComprometidoBatch(productoIds));
    }

    @PostMapping("/calcular-costo")
    public ResponseEntity<CostoComboResponse> calcularCosto(@RequestBody CostoComboRequest request) {
        return ResponseEntity.ok(comboService.calcularCosto(request));
    }
}
