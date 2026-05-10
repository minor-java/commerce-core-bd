package co.com.menor.commerce_core_bd.catalogo.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.commerce_core_bd.catalogo.model.ProductoConCodigos;
import co.com.menor.comun_dto.producto.response.ProductoConCodigosResponse;
import co.com.menor.comun_dto.producto.response.ProductoResponse;

@Component
public class ProductoResponseMapper {

    @Autowired
    private CodigoBarraResponseMapper codigoBarraResponseMapper;

    public ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
            .id(producto.getId())
            .nombre(producto.getNombre())
            .presentacionValor(producto.getPresentacionValor())
            .presentacionUnidad(producto.getPresentacionUnidad())
            .activo(producto.isActivo())
            .fechaCreacion(producto.getFechaCreacion())
            .fechaActualizacion(producto.getFechaActualizacion())
            .usuarioId(producto.getUsuarioId())
            .actualizadoPor(producto.getActualizadoPor())
            .precioVenta(producto.getPrecioVenta())
        .build();
    }

    public ProductoConCodigosResponse toConCodigosResponse(ProductoConCodigos productoConCodigos) {
        Producto producto = productoConCodigos.getProducto();
        return ProductoConCodigosResponse.builder()
            .id(producto.getId())
            .nombre(producto.getNombre())
            .presentacionValor(producto.getPresentacionValor())
            .presentacionUnidad(producto.getPresentacionUnidad())
            .activo(producto.isActivo())
            .fechaCreacion(producto.getFechaCreacion())
            .fechaActualizacion(producto.getFechaActualizacion())
            .usuarioId(producto.getUsuarioId())
            .actualizadoPor(producto.getActualizadoPor())
            .precioVenta(producto.getPrecioVenta())
            .codigos(codigoBarraResponseMapper.toResponseList(productoConCodigos.getCodigos()))
        .build();
    }

    public List<ProductoResponse> toResponseList(List<Producto> productos) {
        return productos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
