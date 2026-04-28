package co.com.menor.commerce_core_bd.compra.repository;

import co.com.menor.commerce_core_bd.compra.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompraRepository extends JpaRepository<Compra, Long>, JpaSpecificationExecutor<Compra> {
}
