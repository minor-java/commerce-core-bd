package co.com.menor.commerce_core_bd.compra.mapper;

import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;
import co.com.menor.comun_dto.compra.response.CompraResponse;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CompraMapper {

    public Compra toEntity(CompraRequest req) {
        Compra compra = new Compra();
        compra.setProveedor(req.getProveedor());
        compra.setObservacion(req.getObservacion());
        compra.setUsuarioId(req.getUsuarioId());
        compra.setFechaCreacion(LocalDateTime.now());
        return compra;
    }

    public CompraResponse toResponse(
        Compra compra,
        List<CompraDetalleResponse> detalles
    ) {

        return new CompraResponse(
            compra.getId(),
            compra.getProveedor(),
            compra.getTotal(),
            compra.getObservacion(),
            compra.getFechaCreacion(),
            compra.getUsuarioId(),
            detalles
        );
    }

    public CompraResponse toResponse(Compra compra) {

        return new CompraResponse(
            compra.getId(),
            compra.getProveedor(),
            compra.getTotal(),
            compra.getObservacion(),
            compra.getFechaCreacion(),
            compra.getUsuarioId(),
            null
        );
    }

}
