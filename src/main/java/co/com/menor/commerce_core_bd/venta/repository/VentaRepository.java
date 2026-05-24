package co.com.menor.commerce_core_bd.venta.repository;

import co.com.menor.commerce_core_bd.venta.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaCreacion >= :inicio AND v.fechaCreacion <= :fin")
    BigDecimal sumTotalByFechaCreacionBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    Long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findTop5ByFechaCreacionBetweenOrderByIdDesc(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
}
