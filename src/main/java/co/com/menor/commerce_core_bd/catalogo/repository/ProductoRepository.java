package co.com.menor.commerce_core_bd.catalogo.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.com.menor.commerce_core_bd.catalogo.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    Optional<Producto> findById(Long id);

    List<Producto> findByActivo(boolean activo);

    boolean existsByNombreAndPresentacionValorAndPresentacionUnidad(
        String nombre,
        BigDecimal presentacionValor,
        String presentacionUnidad
    );
}
