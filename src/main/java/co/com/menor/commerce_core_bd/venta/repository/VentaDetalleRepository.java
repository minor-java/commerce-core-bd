package co.com.menor.commerce_core_bd.venta.repository;

import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {

    List<VentaDetalle> findByVentaId(Long ventaId);
}
