package co.com.menor.commerce_core_bd.catalogo.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.comun_dto.codigo_barras.response.CodigoBarraResponse;

@Component
public class CodigoBarraResponseMapper {

    public CodigoBarraResponse toResponse(CodigoBarra codigoBarra) {
        return CodigoBarraResponse.builder()
            .id(codigoBarra.getId())
            .productoId(codigoBarra.getProductoId())
            .comboId(codigoBarra.getComboId())
            .codigo(codigoBarra.getCodigo())
            .tipo(codigoBarra.getTipo())
            .principal(codigoBarra.isPrincipal())
            .fechaCreacion(codigoBarra.getFechaCreacion())
            .fechaActualizacion(codigoBarra.getFechaActualizacion())
            .usuarioId(codigoBarra.getUsuarioId())
            .actualizadoPor(codigoBarra.getActualizadoPor())
        .build();
    }

    public List<CodigoBarraResponse> toResponseList(List<CodigoBarra> codigosDeBarras) {
        return codigosDeBarras.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
