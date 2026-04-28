package co.com.menor.commerce_core_bd.venta.mapper;

import co.com.menor.commerce_core_bd.venta.model.Venta;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.comun_dto.venta.response.VentaDetalleResponse;
import co.com.menor.comun_dto.venta.response.VentaResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VentaResponseMapper {

    public VentaResponse toResponse(Venta venta, List<VentaDetalle> detalles) {
        if (venta == null) return null;
        return VentaResponse.builder()
                .id(venta.getId())
                .total(venta.getTotal())
                .fechaCreacion(venta.getFechaCreacion())
                .creadoPor(venta.getCreadoPor())
                .detalles(detalles == null ? null : detalles.stream()
                        .map(this::toDetalleResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public VentaDetalleResponse toDetalleResponse(VentaDetalle detalle) {
        if (detalle == null) return null;
        return VentaDetalleResponse.builder()
                .id(detalle.getId())
                .productoId(detalle.getProductoId())
                .ventaId(detalle.getVentaId())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build();
    }
}
