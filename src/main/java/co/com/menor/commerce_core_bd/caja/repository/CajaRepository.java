package co.com.menor.commerce_core_bd.caja.repository;

import co.com.menor.commerce_core_bd.caja.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long>, JpaSpecificationExecutor<Caja> {

    Optional<Caja> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    Optional<Caja> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}
