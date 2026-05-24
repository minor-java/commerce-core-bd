package co.com.menor.commerce_core_bd.venta.mapper;

import co.com.menor.commerce_core_bd.venta.model.Venta;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.comun_dto.venta.request.VentaDetalleRequest;
import co.com.menor.comun_dto.venta.request.VentaRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VentaMapper {

    public Venta toEntity(VentaRequest req) {
        if (req == null) return null;
        Venta venta = new Venta();
        venta.setUsuarioId(req.getUsuarioId());
        venta.setCajaId(req.getCajaId());
        venta.setFechaCreacion(LocalDateTime.now());
        return venta;
    }

    public VentaDetalle toDetalleEntity(VentaDetalleRequest req, Long ventaId) {
        if (req == null) return null;
        VentaDetalle detalle = new VentaDetalle();
        detalle.setProductoId(req.getProductoId());
        detalle.setVentaId(ventaId);
        detalle.setCantidad(req.getCantidad());
        detalle.setPrecioUnitario(req.getPrecioUnitario());
        detalle.setSubtotal(req.getCantidad().multiply(req.getPrecioUnitario()));
        return detalle;
    }
}
