package co.com.menor.commerce_core_bd.catalogo.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.comun_dto.producto.request.FiltroProductoRequest;

public class ProductoSpecification {

    private ProductoSpecification() {}

    public static Specification<Producto> buildFrom(FiltroProductoRequest filtro) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getNombre() != null && !filtro.getNombre().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("nombre")),
                    "%" + filtro.getNombre().trim().toLowerCase() + "%"
                ));
            }

            if (filtro.getPresentacionValor() != null) {
                predicates.add(cb.equal(root.get("presentacionValor"), filtro.getPresentacionValor()));
            }

            if (filtro.getPresentacionUnidad() != null && !filtro.getPresentacionUnidad().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("presentacionUnidad")),
                    "%" + filtro.getPresentacionUnidad().trim().toLowerCase() + "%"
                ));
            }

            if (filtro.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), filtro.getActivo()));
            }

            if (filtro.getPrecioVenta() != null) {
                predicates.add(cb.equal(root.get("precioVenta"), filtro.getPrecioVenta()));
            }

            if (filtro.getCodigoBarra() != null && !filtro.getCodigoBarra().trim().isEmpty()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<CodigoBarra> cbRoot = subquery.from(CodigoBarra.class);
                subquery.select(cbRoot.get("productoId"))
                    .where(cb.like(
                        cb.lower(cbRoot.get("codigo")),
                        "%" + filtro.getCodigoBarra().trim().toLowerCase() + "%"
                    ));
                predicates.add(root.get("Id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
