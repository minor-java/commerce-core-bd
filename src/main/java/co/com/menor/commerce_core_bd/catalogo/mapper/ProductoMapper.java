package co.com.menor.commerce_core_bd.catalogo.mapper;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.comun_dto.producto.request.CreateProductoRequest;
import co.com.menor.comun_dto.producto.request.UpdateProductoRequest;

@Component
public class ProductoMapper {

    public Producto toEntity(CreateProductoRequest req) {
        
        if (req == null) {
            return null;
        }

        Producto producto = new Producto();
        producto.setId(null);
        producto.setNombre(req.getNombre());
        producto.setPresentacionValor(req.getPresentacionValor());
        producto.setPresentacionUnidad(req.getPresentacionUnidad());
        producto.setActivo(req.isActivo());
        producto.setFechaCreacion(req.getFechaCreacion());
        producto.setCreadoPor(req.getCreadoPor());
        producto.setPrecioVenta(req.getPrecioVenta());

        return producto;
    }

    public void updateEntityFromRequest(UpdateProductoRequest req, Producto producto) {
        
        if (req == null || producto == null) return;

        if (req.getNombre() != null) {
            producto.setNombre(req.getNombre());
        }
        if (req.getPresentacionValor() != null) {
            producto.setPresentacionValor(req.getPresentacionValor());
        }
        if (req.getPresentacionUnidad() != null) {
            producto.setPresentacionUnidad(req.getPresentacionUnidad());
        }
        
        producto.setActivo(req.isActivo());
        
        if (req.getFechaActualizacion() != null) {
            producto.setFechaActualizacion(req.getFechaActualizacion());
        }
        if (req.getActualizadoPor() != null) {
            producto.setActualizadoPor(req.getActualizadoPor());
        }
        if (req.getPrecioVenta() != null) {
            producto.setPrecioVenta(req.getPrecioVenta());
        }
    }
}
