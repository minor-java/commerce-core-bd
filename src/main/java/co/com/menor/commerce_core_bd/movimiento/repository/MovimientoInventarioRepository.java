package co.com.menor.commerce_core_bd.movimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long>, JpaSpecificationExecutor<MovimientoInventario> {

    List<MovimientoInventario> findByProductoIdOrderByFechaCreacionAscIdAsc(Long productoId);
    Optional<MovimientoInventario> findById(Long movimientoId);
    Optional<MovimientoInventario> findByReferenciaTipoAndReferenciaId(String referenciaTipo, Long referenciaId);
}
