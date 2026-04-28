package co.com.menor.commerce_core_bd.usuario.service;

import co.com.menor.commerce_core_bd.usuario.mapper.UsuarioMapper;
import co.com.menor.commerce_core_bd.usuario.model.Usuario;
import co.com.menor.commerce_core_bd.usuario.repository.UsuarioRepository;
import co.com.menor.comun_dto.usuario.request.CreateUsuarioRequest;
import co.com.menor.comun_dto.usuario.request.UpdateUsuarioRequest;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Override
    public Optional<Usuario> findById(Long usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        return usuarioRepository.findById(usuarioId);
    }

    @Override
    public Optional<Usuario> findByUsuario(String usuario) {
        if (usuario == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsuario(usuario);
    }

    @Override
    public List<Usuario> allUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public boolean existsByUsuario(String usuario) {
        if (usuario == null) {
            return false;
        }
        return usuarioRepository.existsByUsuario(usuario);
    }

    @Override
    public Usuario saveUsuario(CreateUsuarioRequest dto) {
        Usuario usuarioModel = usuarioMapper.toEntity(dto);
        usuarioModel.setId(null);
        return usuarioRepository.save(usuarioModel);
    }

    @Override
    public Usuario updateUsuario(UpdateUsuarioRequest req) {

        Optional<Usuario> usuarioOpt = findById(req.getId());
    
        if (!usuarioOpt.isPresent()) {
            log.warn("Usuario no encontrado {}", req.getId());
            return null;
        }
    
        Usuario usuario = usuarioOpt.get();
        usuarioMapper.updateEntityFromRequest(req, usuario);
        return usuarioRepository.save(usuario);
    }
}
