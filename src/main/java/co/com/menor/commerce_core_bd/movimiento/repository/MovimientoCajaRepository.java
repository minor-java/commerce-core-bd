package co.com.menor.commerce_core_bd.movimiento.repository;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findByCajaId(Long cajaId);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m " +
           "WHERE m.cajaId = :cajaId AND m.tipo = :tipo AND m.metodoPago = :metodoPago")
    BigDecimal sumMontoByCajaIdAndTipoAndMetodoPago(
            @Param("cajaId") Long cajaId,
            @Param("tipo") String tipo,
            @Param("metodoPago") String metodoPago);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m " +
           "WHERE m.cajaId = :cajaId AND m.tipo = :tipo")
    BigDecimal sumMontoByCajaIdAndTipo(@Param("cajaId") Long cajaId, @Param("tipo") String tipo);
}
