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
@RequestMapping("/caja")
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

    @GetMapping("/obtener-por-caja-id/{id}")
    public ResponseEntity<CajaResponse> obtenerPorCajaId(@PathVariable Long id) {
        CajaResponse caja = cajaService.obtenerPorCajaId(id);
        if (caja == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(caja);
    }

    @GetMapping("/existe-caja/{usuarioId}")
    public ResponseEntity<Boolean> existeCaja(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(cajaService.existeCaja(usuarioId));
    }

    @GetMapping("/obtener/por-usuario/por-estado/{usuarioId}")
    public ResponseEntity<CajaResponse> obtenerPorUsuarioIdPorEstado(@PathVariable Long usuarioId) {
        CajaResponse caja = cajaService.obtenerPorUsuarioIdPorEstado(usuarioId);
        if (caja == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(caja);
    }

    @GetMapping("/tiene-caja-activa/{usuarioId}")
    public ResponseEntity<Boolean> tieneCajaActiva(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(cajaService.tieneCajaActiva(usuarioId));
    }

    @PostMapping("/paginado")
    public ResponseEntity<PaginadoResponse<CajaResponse>> buscarPaginado(
        @RequestBody FiltroCajaRequest filtro
    ) {

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
