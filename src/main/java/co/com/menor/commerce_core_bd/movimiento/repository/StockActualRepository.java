package co.com.menor.commerce_core_bd.movimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.movimiento.model.StockActual;

import java.util.Optional;

@Repository
public interface StockActualRepository extends JpaRepository<StockActual, Long> {

    Optional<StockActual> findByProductoId(Long productoId);
}
