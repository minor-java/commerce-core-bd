package co.com.menor.commerce_core_bd.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.menor.commerce_core_bd.catalogo.mapper.CodigoBarraResponseMapper;
import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.catalogo.service.CodigoBarrasService;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import co.com.menor.comun_dto.codigo_barras.response.CodigoBarraResponse;

@RestController
@RequestMapping("/codigo-barras")
public class CodigoBarraController {

    @Autowired
    private CodigoBarrasService codigoBarrasService;

    @Autowired
    private CodigoBarraResponseMapper codigoBarraResponseMapper;

    @PostMapping("/guardar")
    public ResponseEntity<CodigoBarraResponse> guardarCodigoBarras(
        @RequestBody CreateCondigoBarrasRequest codigoBarras
    ) {

        CodigoBarra guardado = codigoBarrasService.saveCodigoBarras(codigoBarras);

        if (guardado != null) {
            return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(codigoBarraResponseMapper.toResponse(guardado));
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .build();
    }

    @GetMapping("/existe-codigo-barras/{codigoBarras}")
    public ResponseEntity<Boolean> existeUsuario(
        @PathVariable String codigoBarras
    ) {

        boolean existe = codigoBarrasService.existsCodigoBarras(codigoBarras);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/consulta-por-codigo-barras/{codigo}")
    public ResponseEntity<CodigoBarraResponse> buscarPorCodigoBarras(
        @PathVariable String codigo
    ) {

        return codigoBarrasService.findByCodigo(codigo)
            .map(cb -> ResponseEntity.ok(codigoBarraResponseMapper.toResponse(cb)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/consulta-por-producto-id/{id}")
    public ResponseEntity<List<CodigoBarraResponse>> buscarPorNombre(
        @PathVariable Long id
    ) {

        List<CodigoBarra> codigosDeBarras = codigoBarrasService.findByProductoId(id);

        if (codigosDeBarras.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(codigoBarraResponseMapper.toResponseList(codigosDeBarras));
    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        codigoBarrasService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
