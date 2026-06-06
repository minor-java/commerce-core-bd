package co.com.menor.commerce_core_bd.combo.mapper;

import co.com.menor.commerce_core_bd.combo.model.Combo;
import co.com.menor.comun_dto.combo.request.CreateComboRequest;
import co.com.menor.comun_dto.combo.response.ComboDetalleResponse;
import co.com.menor.comun_dto.combo.response.ComboResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class ComboMapper {

    public Combo toEntity(CreateComboRequest req) {
        Combo combo = new Combo();
        combo.setNombre(req.getNombre());
        combo.setDescripcion(req.getDescripcion());
        combo.setPrecioBase(req.getPrecioBase());
        combo.setActivo(true);
        combo.setCantidadDisponible(req.getCantidadDisponible());
        combo.setFechaCreacion(LocalDateTime.now());
        combo.setUsuarioId(req.getUsuarioId());
        return combo;
    }

    public ComboResponse toResponse(Combo combo, List<ComboDetalleResponse> detalles, List<String> codigosBarras) {
        return ComboResponse.builder()
                .id(combo.getId())
                .nombre(combo.getNombre())
                .descripcion(combo.getDescripcion())
                .precioBase(combo.getPrecioBase())
                .activo(combo.getActivo())
                .cantidadDisponible(combo.getCantidadDisponible())
                .fechaCreacion(combo.getFechaCreacion())
                .usuarioId(combo.getUsuarioId())
                .detalles(detalles)
                .codigosBarras(codigosBarras != null ? codigosBarras : Collections.emptyList())
                .build();
    }
}
