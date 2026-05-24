package co.com.menor.commerce_core_bd.usuario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.menor.commerce_core_bd.usuario.mapper.UsuarioResponseMapper;
import co.com.menor.commerce_core_bd.usuario.service.UsuarioService;
import co.com.menor.comun_dto.usuario.response.UsuarioResponse;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioResponseMapper usuarioResponseMapper;

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        return ResponseEntity.ok(
            usuarioResponseMapper.toResponseList(usuarioService.allUsuarios())
        );
    }

    @GetMapping("/existe-usuario/{usuario}")
    public ResponseEntity<Boolean> existeUsuario(
        @PathVariable String usuario
    ) {

        boolean existe = usuarioService.existsByUsuario(usuario);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/consulta-usuario/{usuario}")
    public ResponseEntity<UsuarioResponse> buscarPorUsuario(
        @PathVariable String usuario
    ) {

        return usuarioService.findByUsuario(usuario)
            .map(u -> ResponseEntity.ok(usuarioResponseMapper.toResponse(u)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/consulta-usuario-por-id/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorUsuario(
        @PathVariable("id") Long id
    ) {

        return usuarioService.findById(id)
            .map(u -> ResponseEntity.ok(usuarioResponseMapper.toResponse(u)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
