package co.com.menor.commerce_core_bd.usuario.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.usuario.model.Usuario;
import co.com.menor.comun_dto.usuario.response.UsuarioResponse;

@Component
public class UsuarioResponseMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
            .id(usuario.getId())
            .usuario(usuario.getUsuario())
            .intentos(usuario.getIntentos())
            .activo(usuario.getActivo())
            .fechaCreacion(usuario.getFechaCreacion())
            .fechaActualizacion(usuario.getFechaActualizacion())
            .ultimoLogin(usuario.getUltimoLogin())
            .rol(usuario.getRol())
        .build();
    }

    public List<UsuarioResponse> toResponseList(List<Usuario> usuarios) {
        return usuarios.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
