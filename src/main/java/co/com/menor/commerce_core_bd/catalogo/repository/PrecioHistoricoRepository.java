package co.com.menor.commerce_core_bd.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.menor.commerce_core_bd.catalogo.model.PrecioHistorico;

public interface PrecioHistoricoRepository extends JpaRepository<PrecioHistorico, Long> {
}
