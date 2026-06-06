package co.com.menor.commerce_core_bd.movimiento.controller;

import java.math.BigDecimal;

import co.com.menor.commerce_core_bd.movimiento.service.ReversoService;
import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.request.VentaDetalleReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reversos")
@RequiredArgsConstructor
public class ReversoController {

    private final ReversoService reversoService;

    @PostMapping
    public ResponseEntity<ReversoResponse> crearReverso(@RequestBody ReversoRequest request) {
        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(reversoService.crearReverso(request));
    }

    @PostMapping("/venta-detalle")
    public ResponseEntity<ReversoResponse> crearReversoVentaDetalle(@RequestBody VentaDetalleReversoRequest request) {
        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(reversoService.crearReversoVentaDetalle(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReversoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reversoService.obtenerPorId(id));
    }

    @GetMapping("/cantidad-reversada/{movimientoId}")
    public ResponseEntity<BigDecimal> getCantidadReversada(@PathVariable Long movimientoId) {
        return ResponseEntity.ok(reversoService.getCantidadReversada(movimientoId));
    }

    @GetMapping("/total-hoy")
    public ResponseEntity<BigDecimal> getTotalReversosHoy() {
        return ResponseEntity.ok(reversoService.getTotalReversosHoy());
    }
}
