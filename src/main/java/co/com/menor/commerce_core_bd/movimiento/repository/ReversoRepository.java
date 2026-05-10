package co.com.menor.commerce_core_bd.movimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.movimiento.model.Reverso;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReversoRepository extends JpaRepository<Reverso, Long> {

    List<Reverso> findByMovimientoId(Long movimientoId);

    @Query("SELECT COALESCE(SUM(r.cantidadReversada), 0) FROM Reverso r WHERE r.movimientoId = :movimientoId")
    BigDecimal sumCantidadReversadaByMovimientoId(@Param("movimientoId") Long movimientoId);
}
