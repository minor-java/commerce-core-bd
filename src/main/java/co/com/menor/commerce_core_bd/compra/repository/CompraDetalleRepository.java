package co.com.menor.commerce_core_bd.compra.repository;

import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Long> {

    List<CompraDetalle> findByCompraId(Long compraId);
}
