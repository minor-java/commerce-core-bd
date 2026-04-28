package co.com.menor.commerce_core_bd.usuario.service;

import java.util.List;
import java.util.Optional;

import co.com.menor.commerce_core_bd.usuario.model.Usuario;
import co.com.menor.comun_dto.usuario.request.CreateUsuarioRequest;
import co.com.menor.comun_dto.usuario.request.UpdateUsuarioRequest;

public interface UsuarioService {

    Optional<Usuario> findById(Long usuarioId);

    Optional<Usuario> findByUsuario(String usuario);

    List<Usuario> allUsuarios();

    boolean existsByUsuario(String usuario);

    Usuario saveUsuario(CreateUsuarioRequest req);

    Usuario updateUsuario(UpdateUsuarioRequest req);
}
