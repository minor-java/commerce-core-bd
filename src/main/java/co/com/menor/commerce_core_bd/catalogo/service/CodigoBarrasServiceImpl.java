package co.com.menor.commerce_core_bd.catalogo.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.catalogo.mapper.CodigoBarraMapper;
import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.catalogo.repository.CodigoBarraRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import co.com.menor.comun_dto.codigo_barras.request.EliminarCodigosBarrasRequest;
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
        return codigoBarraRepository.existsByCodigo(codigoBarras);
    }

    @Override
    public Optional<CodigoBarra> findByCodigo(String codigoBarras) {
        return codigoBarraRepository.findByCodigo(codigoBarras);
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
    @Transactional
    public void deleteByIds(EliminarCodigosBarrasRequest codigoBarras) {

        try {

            for (Long id : codigoBarras.getIds()) {
                if (id == null) continue;
                codigoBarraRepository.deleteIfExists(id);
            }
            
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CodigoBarrasService deleteByIds"
            );
        }
    }
}
