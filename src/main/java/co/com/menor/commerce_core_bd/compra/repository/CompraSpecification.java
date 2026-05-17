package co.com.menor.commerce_core_bd.compra.repository;

import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class CompraSpecification {

    private CompraSpecification() {}

    public static Specification<Compra> buildFrom(FiltroCompraRequest filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getProveedor() != null && !filtro.getProveedor().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("proveedor")),
                    "%" + filtro.getProveedor().toLowerCase() + "%"
                ));
            }
            if (filtro.getFechaDesde() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("fechaCreacion"),
                        filtro.getFechaDesde().atStartOfDay()
                    )
                );
            }
            if (filtro.getFechaHasta() != null) {
                predicates.add(
                    cb.lessThan(
                        root.get("fechaCreacion"),
                        filtro.getFechaHasta().plusDays(1).atStartOfDay()
                    )
                );
            }
            if (filtro.getUsuarioId() != null) {
                predicates.add(cb.equal(root.get("usuarioId"), filtro.getUsuarioId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
