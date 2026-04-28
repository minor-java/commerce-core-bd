package co.com.menor.commerce_core_bd.usuario.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.usuario.model.Usuario;

/**
 *
 * @author jeffry
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}
