package co.com.menor.commerce_core_bd.catalogo.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;

@Repository
public interface CodigoBarraRepository extends JpaRepository<CodigoBarra, Long> {

    boolean existsByCodigo(String codigo);

    Optional<CodigoBarra> findByCodigo(String codigo);

    List<CodigoBarra> findByProductoId(Long productoId);

    List<CodigoBarra> findByProductoIdInAndPrincipalTrue(List<Long> productoIds);

    List<CodigoBarra> findByComboId(Long comboId);

    void deleteByComboId(Long comboId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CodigoBarra c WHERE c.id = :id")
    void deleteIfExists(@Param("id") Long id);
}
