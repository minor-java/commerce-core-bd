package co.com.menor.commerce_core_bd.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.catalogo.model.SecuenciaCodigoBarra;

@Repository
public interface SecuenciaCodigoBarraRepository extends JpaRepository<SecuenciaCodigoBarra, Long> {
}
