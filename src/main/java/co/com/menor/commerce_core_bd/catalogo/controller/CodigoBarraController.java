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
import co.com.menor.comun_dto.codigo_barras.request.EliminarCodigosBarrasRequest;
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

        return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .body(codigoBarraResponseMapper.toResponse(guardado));
    }

    @GetMapping("/existe-codigo-barras/{codigoBarras}")
    public ResponseEntity<Boolean> existeUsuario(
        @PathVariable String codigoBarras
    ) {
        return ResponseEntity.ok(
            codigoBarrasService.existsCodigoBarras(codigoBarras)
        );
    }

    @GetMapping("/consulta-por-codigo-barras/{codigo}")
    public ResponseEntity<CodigoBarraResponse> buscarPorCodigoBarras(
        @PathVariable String codigo
    ) {

        CodigoBarra guardado = codigoBarrasService.findByCodigo(codigo).get();

        return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .body(codigoBarraResponseMapper.toResponse(guardado));

    }

    @GetMapping("/consulta-por-producto-id/{id}")
    public ResponseEntity<List<CodigoBarraResponse>> buscarPorNombre(
        @PathVariable Long id
    ) {

        return ResponseEntity.ok(
            codigoBarraResponseMapper.toResponseList(
                codigoBarrasService.findByProductoId(id)
            )
        );
    }

    @DeleteMapping("/delete-by-ids")
    public ResponseEntity<Void> deleteByIds(
        @RequestBody EliminarCodigosBarrasRequest codigoBarras
    ) {

        codigoBarrasService.deleteByIds(codigoBarras);
        return ResponseEntity.ok().build();
    }
}
