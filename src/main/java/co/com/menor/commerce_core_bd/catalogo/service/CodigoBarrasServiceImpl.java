package co.com.menor.commerce_core_bd.catalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.catalogo.mapper.CodigoBarraMapper;
import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.catalogo.repository.CodigoBarraRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CodigoBarrasServiceImpl implements CodigoBarrasService {

    @Autowired
    private CodigoBarraRepository codigoBarraRepository;

    @Autowired
    private CodigoBarraMapper codigoBarraMapper;

    @Override
    public CodigoBarra saveCodigoBarras(CreateCondigoBarrasRequest req) {

        try {
            
            CodigoBarra codigoBarra = codigoBarraMapper.toEntity(req);
            codigoBarra.setId(null);
    
            return codigoBarraRepository.save(codigoBarra);
        } catch (Exception e) {

            throw new MinorExcepcion(
                "ERROR",
                "CodigoBarrasService saveCodigoBarras"
            );
        }
    }

    @Override
    public boolean existsCodigoBarras(String codigoBarras) {
        
        try {
            
            return codigoBarraRepository.existsByCodigo(codigoBarras);
        } catch (Exception e) {
            
            throw new MinorExcepcion(
                "ERROR",
                "CodigoBarrasService existsCodigoBarras"
            );
        }
    }

    @Override
    public Optional<CodigoBarra> findByCodigo(String codigoBarras) {
        
        try {
            
            return codigoBarraRepository.findByCodigo(codigoBarras);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CodigoBarrasService findByCodigo"
            );
        }
    }

    @Override
    public List<CodigoBarra> findByProductoId(Long productoId) {
        try {
            
            return codigoBarraRepository.findByProductoId(productoId);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CodigoBarrasService findByProductoId"
            );
        }
    }

    @Override
    public void deleteById(Long id) {

        try {
            
            codigoBarraRepository.deleteIfExists(id);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CodigoBarrasService deleteById"
            );
        }
    }
}
