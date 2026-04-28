package co.com.menor.commerce_core_bd.compra.mapper;

import co.com.menor.commerce_core_bd.catalogo.mapper.ProductoResponseMapper;
import co.com.menor.commerce_core_bd.catalogo.service.ProductoService;
import co.com.menor.commerce_core_bd.compra.model.Compra;
import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.comun_dto.compra.response.CompraDetalleByIdResponse;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import co.com.menor.comun_dto.producto.response.ProductoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompraResponseMapper {

    private final ProductoService productoService;
    private final ProductoResponseMapper productoResponseMapper;

    public CompraResponse toResponse(Compra compra, List<CompraDetalle> detalles) {
        return new CompraResponse(
            compra.getId(),
            compra.getProveedor(),
            compra.getTotal(),
            compra.getObservacion(),
            compra.getFechaCreacion(),
            compra.getCreadoPor(),
            detalles != null ? toDetalleResponseList(detalles) : Collections.emptyList()
        );
    }

    public List<CompraResponse> toResponseList(List<Compra> compras) {
        return compras.stream()
                .map(c -> toResponse(c, null))
                .collect(Collectors.toList());
    }

    public CompraDetalleByIdResponse toDetalleByIdResponse(CompraDetalle detalle, Compra compra) {
        ProductoResponse producto = productoService.findById(detalle.getProductoId())
                .map(productoResponseMapper::toResponse)
                .orElse(null);
        CompraResponse compraResponse = new CompraResponse(
                compra.getId(),
                compra.getProveedor(),
                compra.getTotal(),
                compra.getObservacion(),
                compra.getFechaCreacion(),
                compra.getCreadoPor(),
                null
        );
        return new CompraDetalleByIdResponse(
                detalle.getId(),
                detalle.getCompraId(),
                detalle.getProductoId(),
                detalle.getCantidad(),
                detalle.getCostoUnitario(),
                detalle.getSubtotal(),
                producto,
                compraResponse
        );
    }

    private List<CompraDetalleResponse> toDetalleResponseList(List<CompraDetalle> detalles) {
        return detalles.stream()
                .map(d -> {
                    ProductoResponse producto = productoService.findById(d.getProductoId())
                            .map(productoResponseMapper::toResponse)
                            .orElse(null);
                    return new CompraDetalleResponse(
                            d.getId(),
                            d.getCompraId(),
                            d.getProductoId(),
                            d.getCantidad(),
                            d.getCostoUnitario(),
                            d.getSubtotal(),
                            producto
                    );
                })
                .collect(Collectors.toList());
    }
}
