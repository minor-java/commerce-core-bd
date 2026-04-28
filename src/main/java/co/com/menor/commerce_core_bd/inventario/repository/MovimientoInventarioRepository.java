package co.com.menor.commerce_core_bd.inventario.repository;

import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long>, JpaSpecificationExecutor<MovimientoInventario> {

    List<MovimientoInventario> findByProductoIdOrderByFechaCreacionAscIdAsc(Long productoId);
}
