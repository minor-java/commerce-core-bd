package co.com.menor.commerce_core_bd.combo.mapper;

import co.com.menor.commerce_core_bd.combo.model.ComboDetalle;
import co.com.menor.comun_dto.combo.request.CreateComboDetalleRequest;
import co.com.menor.comun_dto.combo.response.ComboDetalleResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComboDetalleMapper {

    public ComboDetalle toEntity(CreateComboDetalleRequest req, Long comboId) {
        ComboDetalle detalle = new ComboDetalle();
        detalle.setComboId(comboId);
        detalle.setProductoId(req.getProductoId());
        detalle.setCantidad(req.getCantidad());
        return detalle;
    }

    public ComboDetalleResponse toResponse(ComboDetalle detalle) {
        return ComboDetalleResponse.builder()
                .id(detalle.getId())
                .comboId(detalle.getComboId())
                .productoId(detalle.getProductoId())
                .cantidad(detalle.getCantidad())
                .build();
    }

    public List<ComboDetalleResponse> toResponseList(List<ComboDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return Collections.emptyList();
        }
        return detalles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
