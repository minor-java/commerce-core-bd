package co.com.menor.commerce_core_bd.venta.repository;

import co.com.menor.commerce_core_bd.venta.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {
}
