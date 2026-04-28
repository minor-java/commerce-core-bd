package co.com.menor.commerce_core_bd.inventario.controller;

import co.com.menor.commerce_core_bd.inventario.service.ReversoService;
import co.com.menor.comun_dto.reverso.request.ReversoRequest;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(reversoService.crearReverso(request));
    }
}
