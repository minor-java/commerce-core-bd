package co.com.menor.commerce_core_bd.catalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.catalogo.mapper.CodigoBarraMapper;
import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.commerce_core_bd.catalogo.repository.CodigoBarraRepository;
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

        CodigoBarra codigoBarra = codigoBarraMapper.toEntity(req);
        codigoBarra.setId(null);

        return codigoBarraRepository.save(codigoBarra);
    }

    @Override
    public boolean existsCodigoBarras(String codigoBarras) {
        
        if (codigoBarras == null) {
            return false;
        }

        return codigoBarraRepository.existsByCodigo(codigoBarras);
    }

    @Override
    public Optional<CodigoBarra> findByCodigo(String codigoBarras) {
        
        if (codigoBarras == null) {
            return Optional.empty();
        }
        return codigoBarraRepository.findByCodigo(codigoBarras);
    }

    @Override
    public List<CodigoBarra> findByProductoId(Long productoId) {

        return codigoBarraRepository.findByProductoId(productoId);
    }

    @Override
    public void deleteById(Long id) {
        codigoBarraRepository.deleteIfExists(id);
    }
}
