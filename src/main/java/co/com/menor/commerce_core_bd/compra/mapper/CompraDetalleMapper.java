package co.com.menor.commerce_core_bd.compra.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.comun_dto.compra.request.CompraDetalleRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;
import co.com.menor.comun_dto.producto.response.ProductoResumenResponse;

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

    public CompraDetalleResponse toDetalleResponse(CompraDetalle detalle, String productoNombre) {

        return CompraDetalleResponse.builder()
            .id(detalle.getId())
            .compraId(detalle.getCompraId())
            .producto(ProductoResumenResponse.builder()
                .productoId(detalle.getProductoId())
                .nombre(productoNombre)
                .build())
            .cantidad(detalle.getCantidad())
            .costoUnitario(detalle.getCostoUnitario())
            .subtotal(detalle.getSubtotal())
            .build();
    }

    public List<CompraDetalleResponse> toDetalleResponseList(
        List<CompraDetalle> detalles,
        Map<Long, String> productoNombres
    ) {

        if (detalles == null || detalles.isEmpty()) {
            return Collections.emptyList();
        }

        return detalles.stream()
            .map(d -> toDetalleResponse(d, productoNombres.get(d.getProductoId())))
            .collect(Collectors.toList());
    }
}
