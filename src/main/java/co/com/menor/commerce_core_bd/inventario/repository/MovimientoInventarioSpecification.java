package co.com.menor.commerce_core_bd.inventario.repository;

import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import co.com.menor.comun_dto.inventario.request.FiltroMovimientoInventarioRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioSpecification {

    private MovimientoInventarioSpecification() {}

    public static Specification<MovimientoInventario> buildFrom(FiltroMovimientoInventarioRequest filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getProductoId() != null) {
                predicates.add(cb.equal(root.get("productoId"), filtro.getProductoId()));
            }
            if (filtro.getNombreProducto() != null && !filtro.getNombreProducto().trim().isEmpty()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Producto> productoRoot = subquery.from(Producto.class);
                subquery.select(productoRoot.get("Id"))
                        .where(cb.like(
                                cb.lower(productoRoot.get("nombre")),
                                "%" + filtro.getNombreProducto().trim().toLowerCase() + "%"
                        ));
                predicates.add(root.get("productoId").in(subquery));
            }
            if (filtro.getTipo() != null && !filtro.getTipo().trim().isEmpty()) {
                predicates.add(cb.equal(
                    cb.lower(root.get("tipo")),
                    filtro.getTipo().trim().toLowerCase()
                ));
            }
            if (filtro.getReferenciaTipo() != null && !filtro.getReferenciaTipo().trim().isEmpty()) {
                predicates.add(cb.equal(
                    cb.lower(root.get("referenciaTipo")),
                    filtro.getReferenciaTipo().trim().toLowerCase()
                ));
            }
            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaDesde()));
            }
            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaHasta()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
