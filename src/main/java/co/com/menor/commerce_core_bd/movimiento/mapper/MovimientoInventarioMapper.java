package co.com.menor.commerce_core_bd.movimiento.mapper;

import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.comun_dto.inventario.request.CreateMovimientoInventarioRequest;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;

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
        entity.setUsuarioId(req.getUsuarioId());
        entity.setMovimientoOrigenId(req.getMovimientoOrigenId());
        return entity;
    }

    public MovimientoInventarioResponse toResponse(MovimientoInventario entity) {
        
        if (entity == null) return null;
        
        return MovimientoInventarioResponse.builder()
            .id(entity.getId())
            .productoId(entity.getProductoId())
            .tipo(entity.getTipo())
            .cantidad(entity.getCantidad())
            .costoUnitario(entity.getCostoUnitario())
            .costoTotal(entity.getCostoTotal())
            .referenciaTipo(entity.getReferenciaTipo())
            .referenciaId(entity.getReferenciaId())
            .fechaCreacion(entity.getFechaCreacion())
            .usurioId(entity.getUsuarioId())
            .movimientoOrigenId(entity.getMovimientoOrigenId())
        .build();
    }
}
