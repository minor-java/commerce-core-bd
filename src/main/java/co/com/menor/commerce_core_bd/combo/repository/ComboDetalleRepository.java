package co.com.menor.commerce_core_bd.combo.repository;

import co.com.menor.commerce_core_bd.combo.model.ComboDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ComboDetalleRepository extends JpaRepository<ComboDetalle, Long> {

    List<ComboDetalle> findByComboId(Long comboId);

    void deleteByComboId(Long comboId);

    @Query(value =
           "SELECT COALESCE(SUM(cd.cantidad * c.cantidad_disponible), 0) " +
           "FROM COMBO_DETALLE cd " +
           "JOIN COMBO c ON cd.combo_id = c.id " +
           "WHERE cd.producto_id = :productoId " +
           "AND c.activo = TRUE " +
           "AND c.cantidad_disponible > 0",
           nativeQuery = true)
    BigDecimal sumCantidadByProductoIdAndComboActivo(@Param("productoId") Long productoId);

    @Query(value =
           "SELECT cd.producto_id, SUM(cd.cantidad * c.cantidad_disponible) " +
           "FROM COMBO_DETALLE cd " +
           "JOIN COMBO c ON cd.combo_id = c.id " +
           "WHERE cd.producto_id IN :productoIds " +
           "AND c.activo = TRUE " +
           "AND c.cantidad_disponible > 0 " +
           "GROUP BY cd.producto_id",
           nativeQuery = true)
    List<Object[]> sumCantidadGroupByProductoIdForActiveCombos(@Param("productoIds") List<Long> productoIds);
}
