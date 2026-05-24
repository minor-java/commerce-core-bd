package co.com.menor.commerce_core_bd.auth.service;

import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.usuario.model.Usuario;
import co.com.menor.commerce_core_bd.usuario.service.UsuarioService;
import co.com.menor.comun_dto.auth.request.AuthRequest;
import co.com.menor.comun_dto.auth.response.AuthResponse;
import co.com.menor.security_core.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(AuthRequest request) {
        Usuario usuario = usuarioService.findByUsuario(request.getUsuario())
            .orElseThrow(() -> new MinorExcepcion("CREDENCIALES_INVALIDAS", "Credenciales inválidas"));

        if (!usuario.getActivo()) {
            throw new MinorExcepcion("USUARIO_INACTIVO", "Usuario inactivo");
        }

        String contrasena = new String(Base64.getDecoder().decode(request.getContrasena()));

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            int intentos = usuario.getIntentos() != null ? usuario.getIntentos() : 0;
            usuario.setIntentos(intentos + 1);
            usuarioService.save(usuario);
            throw new MinorExcepcion("CREDENCIALES_INVALIDAS", "Credenciales inválidas");
        }

        usuario.setIntentos(0);
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioService.save(usuario);

        Map<String, Object> claims = new HashMap<>();
        claims.put("usuario", usuario.getUsuario());
        claims.put("rol", usuario.getRol());

        String token = jwtProvider.generateToken(usuario.getId().toString(), claims);

        return new AuthResponse(usuario.getId(), usuario.getUsuario(), usuario.getRol(), usuario.getActivo(), token);
    }
}
