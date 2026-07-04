package co.com.menor.commerce_core_bd.catalogo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.catalogo.model.AreaNegocio;
import co.com.menor.commerce_core_bd.catalogo.repository.AreaNegocioRepository;

@Service
public class AreaNegocioServiceImpl implements AreaNegocioService {

    @Autowired
    private AreaNegocioRepository areaNegocioRepository;

    @Override
    public List<AreaNegocio> findActivos() {
        return areaNegocioRepository.findByActivoTrue();
    }
}
