package co.com.menor.commerce_core_bd.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.menor.commerce_core_bd.catalogo.model.AreaNegocio;

public interface AreaNegocioRepository extends JpaRepository<AreaNegocio, Long> {

    List<AreaNegocio> findByActivoTrue();
}
