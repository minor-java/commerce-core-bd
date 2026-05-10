package co.com.menor.commerce_core_bd.movimiento.mapper;

import co.com.menor.commerce_core_bd.movimiento.model.Reverso;
import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReversoMapper {

    public Reverso toEntity(ReversoRequest req) {
        if (req == null) return null;
        Reverso reverso = new Reverso();
        reverso.setMovimientoId(req.getMovimientoId());
        reverso.setCantidadReversada(req.getCantidadReversada());
        reverso.setObservacion(req.getObservacion());
        reverso.setUsuarioId(req.getUsuarioId());
        reverso.setFechaCreacion(LocalDateTime.now());
        return reverso;
    }

    public ReversoResponse toResponse(Reverso entity) {
        if (entity == null) return null;
        return ReversoResponse.builder()
            .id(entity.getId())
            .movimientoId(entity.getMovimientoId())
            .cantidadReversada(entity.getCantidadReversada())
            .observacion(entity.getObservacion())
            .fechaCreacion(entity.getFechaCreacion())
            .usuarioId(entity.getUsuarioId())
        .build();
    }
}
