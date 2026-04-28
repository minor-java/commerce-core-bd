package co.com.menor.commerce_core_bd.inventario.repository;

import co.com.menor.commerce_core_bd.inventario.model.StockActual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockActualRepository extends JpaRepository<StockActual, Long> {

    Optional<StockActual> findByProductoId(Long productoId);
}
