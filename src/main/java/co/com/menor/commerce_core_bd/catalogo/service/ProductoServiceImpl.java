package co.com.menor.commerce_core_bd.catalogo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import co.com.menor.comun_dto.producto.request.CreateProductoRequest;
import co.com.menor.comun_dto.producto.request.ExistsProductoRequest;
import co.com.menor.comun_dto.producto.request.FiltroProductoRequest;
import co.com.menor.comun_dto.producto.request.UpdateProductoRequest;
import co.com.menor.comun_dto.utils.CodigoBarrasConstants;
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

        try {
            
            return productoRepository.findById(productoId);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService findById"
            );
        }
    }

    @Override
    public List<Producto> findByLikeNombre(String nombre) {

        try {
            
            return productoRepository.findByNombreContainingIgnoreCase(nombre);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService findByLikeNombre"
            );
        }
    }

    @Override
    public List<Producto> allProductos() {

        try {
            
            return productoRepository.findAll();
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService allProductos"
            );
        }
    }

    @Override
    public boolean existsProducto(ExistsProductoRequest existsProductoRequest) {
        
        try {
            
            return productoRepository.existsByNombreAndPresentacionValorAndPresentacionUnidad(
                existsProductoRequest.getNombre(),
                existsProductoRequest.getPresentacionValor(),
                existsProductoRequest.getPresentacionUnidad()
            );
        } catch (Exception e) {
            
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService existsProducto"
            );
        }
    }

    @Override
    @Transactional
    public ProductoConCodigos saveProducto(CreateProductoRequest req) {

        try {
            
            Producto productoModel = productoMapper.toEntity(req);
            productoModel.setId(null);
            Producto productoGuardado = productoRepository.save(productoModel);
    
            List<CodigoBarra> codigosGuardados = Collections.emptyList();
    
            if (req.getCodigosBarras() != null && !req.getCodigosBarras().isEmpty()) {
                
                guardarCodigosBarras(
                    productoGuardado.getId(),
                    req.getUsuarioId(),
                    req.getCodigosBarras()
                );
            }
    
            if (productoGuardado.getPrecioVenta() != null) {
                precioHistoricoRepository.save(PrecioHistorico.builder()
                    .productoId(productoGuardado.getId())
                    .precioAnterior(null)
                    .precioNuevo(productoGuardado.getPrecioVenta())
                    .fechaCreacion(productoGuardado.getFechaCreacion())
                    .usuarioId(productoGuardado.getUsuarioId())
                .build());
            }
    
            return new ProductoConCodigos(productoGuardado, codigosGuardados);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService saveProducto"
            );
        }
    }

    private void guardarCodigosBarras(Long productoId, Long usuarioId, List<String> codigos) {

        if (codigos == null || codigos.isEmpty()) {
            return;
        }

        List<String> codigosValidos = codigos.stream()
            .filter(c -> c != null && !c.trim().isEmpty())
            .collect(Collectors.toList());

        for (int i = 0; i < codigosValidos.size(); i++) {

            String codigo = codigosValidos.get(i);

            CreateCondigoBarrasRequest cbReq = new CreateCondigoBarrasRequest();
            cbReq.setProductoId(productoId);
            cbReq.setCodigo(codigo);
            cbReq.setTipo(CodigoBarrasConstants.TIPO_EAN_13);
            cbReq.setFechaCreacion(LocalDateTime.now());
            cbReq.setUsuarioId(usuarioId);

            guardarCodigo(cbReq);
        }
    }

    private CodigoBarra guardarCodigo(
        CreateCondigoBarrasRequest codigoReq
    ) {

        try {
            
            CodigoBarra codigoBarra = codigoBarraMapper.toEntity(codigoReq);
            codigoBarra.setId(null);
            return codigoBarraRepository.save(codigoBarra);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService guardarCodigo"
            );
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            
            productoRepository.deleteById(id);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService deleteById"
            );
        }
    }

    @Override
    public Page<Producto> buscarPaginado(FiltroProductoRequest filtro) {
        
        try {
            
            PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
            return productoRepository.findAll(ProductoSpecification.buildFrom(filtro), pageable);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "ProductoService buscarPaginado"
            );
        }
    }

    @Override
    @Transactional
    public Producto updateProducto(UpdateProductoRequest req) {

        try {
            
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
                .usuarioId(productoActualizado.getActualizadoPor() != null
                        ? productoActualizado.getActualizadoPor()
                        : productoActualizado.getUsuarioId())
                .build());
            }
    
            return productoActualizado;
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
            "ProductoService updateProducto"
            );
        }
    }
}
