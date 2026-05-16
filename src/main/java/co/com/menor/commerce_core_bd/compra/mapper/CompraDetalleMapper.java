package co.com.menor.commerce_core_bd.compra.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.comun_dto.compra.request.CompraDetalleRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;

@Component
public class CompraDetalleMapper {

    public CompraDetalle toDetalleEntity(
        CompraDetalleRequest req, 
        Long compraId, 
        Long usuarioId
    ) {

        CompraDetalle detalle = new CompraDetalle();
        detalle.setCompraId(compraId);
        detalle.setProductoId(req.getProductoId());
        detalle.setCantidad(req.getCantidad());
        detalle.setCostoUnitario(req.getCostoUnitario());
        detalle.setSubtotal(req.getCantidad().multiply(req.getCostoUnitario()));
        detalle.setUsuarioId(usuarioId);
        
        return detalle;
    }

    public CompraDetalleResponse toDetalleResponse(CompraDetalle detalle) {
        
        return new CompraDetalleResponse(
            detalle.getId(),
            detalle.getCompraId(),
            detalle.getProductoId(),
            detalle.getCantidad(),
            detalle.getCostoUnitario(),
            detalle.getSubtotal()
        );
    }

    public List<CompraDetalleResponse> toDetalleResponseList(
        List<CompraDetalle> detalles
    ) {

        if (detalles == null || detalles.isEmpty()) {
            return Collections.emptyList();
        }
    
        return detalles.stream()
        .map(this::toDetalleResponse)
        .collect(Collectors.toList());
    }
}
