package co.com.menor.commerce_core_bd.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.menor.commerce_core_bd.catalogo.mapper.ProductoResponseMapper;
import co.com.menor.commerce_core_bd.catalogo.model.Producto;
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

        return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .body(
            productoResponseMapper.toConCodigosResponse(
                productoService.saveProducto(productoRequest)
            )
        );
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
        return ResponseEntity.ok(
            productoResponseMapper.toResponseList(
                productoService.findByLikeNombre(nombre.getNombre())
            )
        );
    }

    @PostMapping("/existe-producto")
    public ResponseEntity<Boolean> existeUsuario(
        @RequestBody ExistsProductoRequest existsProductoRequest
    ) {

        return ResponseEntity.ok(
            productoService.existsProducto(existsProductoRequest)
        );
    }    

    @PutMapping("/actualizar")
    public ResponseEntity<ProductoResponse> actualizarUsuario(
        @RequestBody UpdateProductoRequest req
    ) {
        return ResponseEntity.ok(
            productoResponseMapper.toResponse(
                productoService.updateProducto(req)
            )
        );
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

        return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .body(
            productoResponseMapper.toResponse(
                productoService.findById(id).get()
            )
        );
    }

}
