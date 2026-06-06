package co.com.menor.commerce_core_bd.combo.repository;

import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.combo.model.Combo;
import co.com.menor.comun_dto.combo.request.FiltroComboRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public class ComboSpecification {

    private ComboSpecification() {}

    public static Specification<Combo> buildFrom(FiltroComboRequest filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getNombre() != null && !filtro.getNombre().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("nombre")),
                        "%" + filtro.getNombre().toLowerCase() + "%"
                ));
            }

            if (filtro.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), filtro.getActivo()));
            }

            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("fechaCreacion"),
                        filtro.getFechaDesde().atStartOfDay()
                ));
            }

            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThan(
                        root.get("fechaCreacion"),
                        filtro.getFechaHasta().plusDays(1).atStartOfDay()
                ));
            }

            if (filtro.getUsuarioId() != null) {
                predicates.add(cb.equal(root.get("usuarioId"), filtro.getUsuarioId()));
            }

            if (filtro.getCodigoBarra() != null && !filtro.getCodigoBarra().trim().isEmpty()) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<CodigoBarra> cbRoot = sub.from(CodigoBarra.class);
                sub.select(cbRoot.get("comboId"))
                   .where(cb.equal(cbRoot.get("codigo"), filtro.getCodigoBarra().trim()));
                predicates.add(root.get("id").in(sub));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
