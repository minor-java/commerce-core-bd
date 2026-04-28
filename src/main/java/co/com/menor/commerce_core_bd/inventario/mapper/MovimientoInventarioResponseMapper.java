package co.com.menor.commerce_core_bd.inventario.mapper;

import co.com.menor.comun_dto.compra.response.CompraDetalleByIdResponse;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioDetalladoResponse;
import co.com.menor.comun_dto.inventario.response.MovimientoInventarioResponse;
import co.com.menor.commerce_core_bd.inventario.model.MovimientoInventario;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioResponseMapper {

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
                .creadoPor(entity.getCreadoPor())
                .movimientoOrigenId(entity.getMovimientoOrigenId())
                .build();
    }

    public MovimientoInventarioDetalladoResponse toDetalladoResponse(
            MovimientoInventario entity, CompraDetalleByIdResponse detalleCompra) {
        if (entity == null) return null;
        return MovimientoInventarioDetalladoResponse.builder()
                .id(entity.getId())
                .productoId(entity.getProductoId())
                .tipo(entity.getTipo())
                .cantidad(entity.getCantidad())
                .costoUnitario(entity.getCostoUnitario())
                .costoTotal(entity.getCostoTotal())
                .referenciaTipo(entity.getReferenciaTipo())
                .referenciaId(entity.getReferenciaId())
                .fechaCreacion(entity.getFechaCreacion())
                .creadoPor(entity.getCreadoPor())
                .movimientoOrigenId(entity.getMovimientoOrigenId())
                .detalleCompra(detalleCompra)
                .build();
    }
}
