package co.com.menor.commerce_core_bd.inventario.mapper;

import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioMapper {

    public MovimientoInventario toEntity(CreateMovimientoInventarioRequest req) {
        if (req == null) return null;
        MovimientoInventario entity = new MovimientoInventario();
        entity.setProductoId(req.getProductoId());
        entity.setTipo(req.getTipo());
        entity.setCantidad(req.getCantidad());
        entity.setCostoUnitario(req.getCostoUnitario());
        entity.setReferenciaTipo(req.getReferenciaTipo());
        entity.setReferenciaId(req.getReferenciaId());
        entity.setCreadoPor(req.getCreadoPor());
        entity.setMovimientoOrigenId(req.getMovimientoOrigenId());
        return entity;
    }
}
