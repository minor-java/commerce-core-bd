package co.com.menor.commerce_core_bd.compra.mapper;

import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.comun_dto.compra.request.CompraDetalleRequest;
import co.com.menor.comun_dto.compra.request.CompraRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CompraMapper {

    public Compra toEntity(CompraRequest req) {
        Compra compra = new Compra();
        compra.setProveedor(req.getProveedor());
        compra.setObservacion(req.getObservacion());
        compra.setCreadoPor(req.getCreadoPor());
        compra.setFechaCreacion(LocalDateTime.now());
        return compra;
    }

    public CompraDetalle toDetalleEntity(CompraDetalleRequest req, Long compraId, Long creadoPor) {
        CompraDetalle detalle = new CompraDetalle();
        detalle.setCompraId(compraId);
        detalle.setProductoId(req.getProductoId());
        detalle.setCantidad(req.getCantidad());
        detalle.setCostoUnitario(req.getCostoUnitario());
        detalle.setSubtotal(req.getCantidad().multiply(req.getCostoUnitario()));
        detalle.setCreadoPor(creadoPor);
        detalle.setFechaCreacion(LocalDateTime.now());
        return detalle;
    }
}
