package co.com.menor.commerce_core_bd.catalogo.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.catalogo.model.AreaNegocio;
import co.com.menor.comun_dto.area_negocio.response.AreaNegocioResponse;

@Component
public class AreaNegocioResponseMapper {

    public AreaNegocioResponse toResponse(AreaNegocio areaNegocio) {
        return AreaNegocioResponse.builder()
            .id(areaNegocio.getId())
            .nombre(areaNegocio.getNombre())
            .activo(areaNegocio.isActivo())
        .build();
    }

    public List<AreaNegocioResponse> toResponseList(List<AreaNegocio> areas) {
        return areas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
