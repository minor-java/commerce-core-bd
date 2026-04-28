package co.com.menor.commerce_core_bd.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.menor.commerce_core_bd.catalogo.mapper.ProductoResponseMapper;
import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.commerce_core_bd.catalogo.model.ProductoConCodigos;
import co.com.menor.commerce_core_bd.catalogo.service.ProductoService;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import co.com.menor.comun_dto.producto.request.CreateProductoRequest;
import co.com.menor.comun_dto.producto.request.ExistsProductoRequest;
import co.com.menor.comun_dto.producto.request.FiltroProductoRequest;
import co.com.menor.comun_dto.producto.request.FindProductoByNombreRequest;
import co.com.menor.comun_dto.producto.request.UpdateProductoRequest;
import co.com.menor.comun_dto.producto.response.ProductoConCodigosResponse;
import co.com.menor.comun_dto.producto.response.ProductoResponse;

@RestController
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoResponseMapper productoResponseMapper;

    @PostMapping("/guardar")
    public ResponseEntity<ProductoConCodigosResponse> guardarProducto(
        @RequestBody CreateProductoRequest productoRequest
    ) {

        ProductoConCodigos guardado = productoService.saveProducto(productoRequest);

        if (guardado != null) {
            return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(productoResponseMapper.toConCodigosResponse(guardado));
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .build();
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoResponse>> getAllProductos() {
        return ResponseEntity.ok(
            productoResponseMapper.toResponseList(productoService.allProductos())
        );
    }

    @PostMapping("/consulta-por-nombre")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombre(
        @RequestBody FindProductoByNombreRequest nombre
    ) {

        List<Producto> productos = productoService.findByLikeNombre(nombre.getNombre());

        if (productos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(productoResponseMapper.toResponseList(productos));
    }

    @PostMapping("/existe-producto")
    public ResponseEntity<Boolean> existeUsuario(
        @RequestBody ExistsProductoRequest existsProductoRequest
    ) {

        boolean existe = productoService.existsProducto(existsProductoRequest);
        return ResponseEntity.ok(existe);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/actualizar")
    public ResponseEntity<ProductoResponse> actualizarUsuario(
        @RequestBody UpdateProductoRequest req
    ) {

        Producto actualizado = productoService.updateProducto(req);

        if (actualizado != null) {
            return ResponseEntity.ok(productoResponseMapper.toResponse(actualizado));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/paginado")
    public ResponseEntity<PaginadoResponse<ProductoResponse>> buscarPaginado(
        @RequestBody FiltroProductoRequest filtro
    ) {
        Page<Producto> page = productoService.buscarPaginado(filtro);
        PaginadoResponse<ProductoResponse> respuesta = new PaginadoResponse<>(
            productoResponseMapper.toResponseList(page.getContent()),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages()
        );
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/producto-by-id/{id}")
    public ResponseEntity<ProductoResponse> getProductosById(@PathVariable Long id) {

        return productoService.findById(id)
            .map(productoResponseMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
