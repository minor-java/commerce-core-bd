package co.com.menor.commerce_core_bd.catalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.commerce_core_bd.catalogo.model.ProductoConCodigos;
import co.com.menor.comun_dto.producto.request.CreateProductoRequest;
import co.com.menor.comun_dto.producto.request.ExistsProductoRequest;
import co.com.menor.comun_dto.producto.request.FiltroProductoRequest;
import co.com.menor.comun_dto.producto.request.UpdateProductoRequest;

public interface ProductoService {

    Optional<Producto> findById(Long productoId);

    List<Producto> findByLikeNombre(String nombre);

    List<Producto> allProductos();

    boolean existsProducto(ExistsProductoRequest existsProductoRequest);

    ProductoConCodigos saveProducto(CreateProductoRequest req);

    Producto updateProducto(UpdateProductoRequest req);

    void deleteById(Long id);

    Page<Producto> buscarPaginado(FiltroProductoRequest filtro);
}
