package co.com.menor.commerce_core_bd.caja.controller;

import co.com.menor.commerce_core_bd.caja.service.CajaService;
import co.com.menor.comun_dto.caja.request.AbrirCajaRequest;
import co.com.menor.comun_dto.caja.request.CerrarCajaRequest;
import co.com.menor.comun_dto.caja.request.FiltroCajaRequest;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cajas")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @PostMapping("/abrir")
    public ResponseEntity<CajaResponse> abrir(@RequestBody AbrirCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrirCaja(request));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<CajaResponse> cerrar(@RequestBody CerrarCajaRequest request) {
        return ResponseEntity.ok(cajaService.cerrarCaja(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.obtenerPorId(id));
    }

    @GetMapping("/abierta/{usuarioId}")
    public ResponseEntity<CajaResponse> obtenerCajaAbierta(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(cajaService.obtenerCajaAbierta(usuarioId));
    }

    @PostMapping("/paginado")
    public ResponseEntity<PaginadoResponse<CajaResponse>> buscarPaginado(
            @RequestBody FiltroCajaRequest filtro) {
        Page<CajaResponse> page = cajaService.buscarPaginado(filtro);
        PaginadoResponse<CajaResponse> respuesta = new PaginadoResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(respuesta);
    }
}
