package co.com.menor.commerce_core_bd.catalogo.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.catalogo.mapper.CodigoBarraMapper;
import co.com.menor.commerce_core_bd.catalogo.mapper.ProductoMapper;
import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.catalogo.model.PrecioHistorico;
import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.commerce_core_bd.catalogo.model.ProductoConCodigos;
import co.com.menor.commerce_core_bd.catalogo.repository.CodigoBarraRepository;
import co.com.menor.commerce_core_bd.catalogo.repository.PrecioHistoricoRepository;
import co.com.menor.commerce_core_bd.catalogo.repository.ProductoRepository;
import co.com.menor.commerce_core_bd.catalogo.repository.ProductoSpecification;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import co.com.menor.comun_dto.producto.request.CreateProductoRequest;
import co.com.menor.comun_dto.producto.request.ExistsProductoRequest;
import co.com.menor.comun_dto.producto.request.FiltroProductoRequest;
import co.com.menor.comun_dto.producto.request.UpdateProductoRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CodigoBarraRepository codigoBarraRepository;

    @Autowired
    private ProductoMapper productoMapper;

    @Autowired
    private CodigoBarraMapper codigoBarraMapper;

    @Autowired
    private PrecioHistoricoRepository precioHistoricoRepository;

    @Override
    public Optional<Producto> findById(Long productoId) {

        if (productoId == null) {
            return Optional.empty();
        }

        return productoRepository.findById(productoId);
    }

    @Override
    public List<Producto> findByLikeNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public List<Producto> allProductos() {
        return productoRepository.findAll();
    }

    @Override
    public boolean existsProducto(ExistsProductoRequest existsProductoRequest) {

        if (existsProductoRequest == null) {
            return false;
        }

        return productoRepository.existsByNombreAndPresentacionValorAndPresentacionUnidad(
            existsProductoRequest.getNombre(),
            existsProductoRequest.getPresentacionValor(),
            existsProductoRequest.getPresentacionUnidad()
        );
    }

    @Override
    @Transactional
    public ProductoConCodigos saveProducto(CreateProductoRequest req) {

        Producto productoModel = productoMapper.toEntity(req);
        productoModel.setId(null);
        Producto productoGuardado = productoRepository.save(productoModel);

        List<CodigoBarra> codigosGuardados = Collections.emptyList();

        if (req.getCodigos() != null && !req.getCodigos().isEmpty()) {
            codigosGuardados = req.getCodigos().stream()
                .map(codigoReq -> guardarCodigo(codigoReq, productoGuardado.getId()))
                .collect(Collectors.toList());
        }

        if (productoGuardado.getPrecioVenta() != null) {
            precioHistoricoRepository.save(PrecioHistorico.builder()
                .productoId(productoGuardado.getId())
                .precioAnterior(null)
                .precioNuevo(productoGuardado.getPrecioVenta())
                .fechaCreacion(productoGuardado.getFechaCreacion())
                .creadoPor(productoGuardado.getCreadoPor())
            .build());
        }

        return new ProductoConCodigos(productoGuardado, codigosGuardados);
    }

    private CodigoBarra guardarCodigo(CreateCondigoBarrasRequest codigoReq, Long productoId) {
        CodigoBarra codigoBarra = codigoBarraMapper.toEntity(codigoReq);
        codigoBarra.setId(null);
        codigoBarra.setProductoId(productoId);
        return codigoBarraRepository.save(codigoBarra);
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public Page<Producto> buscarPaginado(FiltroProductoRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        return productoRepository.findAll(ProductoSpecification.buildFrom(filtro), pageable);
    }

    @Override
    @Transactional
    public Producto updateProducto(UpdateProductoRequest req) {

        Optional<Producto> productoOpt = findById(req.getId());

        if (!productoOpt.isPresent()) {
            log.warn("Producto no encontrado {}", req.getId());
            return null;
        }

        Producto producto = productoOpt.get();
        BigDecimal precioAnterior = producto.getPrecioVenta();

        productoMapper.updateEntityFromRequest(req, producto);
        Producto productoActualizado = productoRepository.save(producto);

        BigDecimal precioNuevo = productoActualizado.getPrecioVenta();
        boolean precioChanged = precioNuevo != null
                && (precioAnterior == null || precioAnterior.compareTo(precioNuevo) != 0);

        if (precioChanged) {
            precioHistoricoRepository.save(PrecioHistorico.builder()
                .productoId(productoActualizado.getId())
                .precioAnterior(precioAnterior)
                .precioNuevo(precioNuevo)
                .fechaCreacion(productoActualizado.getFechaActualizacion() != null
                        ? productoActualizado.getFechaActualizacion()
                        : java.time.LocalDateTime.now())
                .creadoPor(productoActualizado.getActualizadoPor() != null
                        ? productoActualizado.getActualizadoPor()
                        : productoActualizado.getCreadoPor())
                .build());
        }

        return productoActualizado;
    }
}
