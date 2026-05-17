package co.com.menor.commerce_core_bd.movimiento.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.movimiento.model.StockActual;
import co.com.menor.comun_dto.inventario.response.StockPaginadoResponse;

@Repository
public interface StockActualRepository extends JpaRepository<StockActual, Long> {

    Optional<StockActual> findByProductoId(Long productoId);

    @Query(
        value =
            "SELECT new co.com.menor.comun_dto.inventario.response.StockPaginadoResponse(" +
                "sa.productoId, p.nombre, p.presentacionValor, p.presentacionUnidad, p.activo, p.precioVenta, sa.stock, sa.costoPromedio" +
            ") " +
            "FROM StockActual sa, Producto p " +
            "WHERE p.Id = sa.productoId " +
            "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', LOWER(:nombre), '%')) " +
            "AND (:presentacionValor IS NULL OR p.presentacionValor = :presentacionValor) " +
            "AND (:presentacionUnidad IS NULL OR LOWER(p.presentacionUnidad) LIKE CONCAT('%', LOWER(:presentacionUnidad), '%')) " +
            "AND (:activo IS NULL OR p.activo = :activo) " +
            "AND (:precioVenta IS NULL OR p.precioVenta = :precioVenta)",
        countQuery =
            "SELECT COUNT(sa.productoId) " +
            "FROM StockActual sa, Producto p " +
            "WHERE p.Id = sa.productoId " +
            "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', LOWER(:nombre), '%')) " +
            "AND (:presentacionValor IS NULL OR p.presentacionValor = :presentacionValor) " +
            "AND (:presentacionUnidad IS NULL OR LOWER(p.presentacionUnidad) LIKE CONCAT('%', LOWER(:presentacionUnidad), '%')) " +
            "AND (:activo IS NULL OR p.activo = :activo) " +
            "AND (:precioVenta IS NULL OR p.precioVenta = :precioVenta)"
    )
    Page<StockPaginadoResponse> findStockPaginado(
        @Param("nombre") String nombre,
        @Param("presentacionValor") BigDecimal presentacionValor,
        @Param("presentacionUnidad") String presentacionUnidad,
        @Param("activo") Boolean activo,
        @Param("precioVenta") BigDecimal precioVenta,
        Pageable pageable
    );
}
