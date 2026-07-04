package co.com.menor.commerce_core_bd.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.menor.commerce_core_bd.catalogo.mapper.AreaNegocioResponseMapper;
import co.com.menor.commerce_core_bd.catalogo.service.AreaNegocioService;
import co.com.menor.comun_dto.area_negocio.response.AreaNegocioResponse;

@RestController
@RequestMapping("/area-negocio")
public class AreaNegocioController {

    @Autowired
    private AreaNegocioService areaNegocioService;

    @Autowired
    private AreaNegocioResponseMapper areaNegocioResponseMapper;

    @GetMapping("/activos")
    public ResponseEntity<List<AreaNegocioResponse>> findActivos() {
        return ResponseEntity.ok(
            areaNegocioResponseMapper.toResponseList(areaNegocioService.findActivos())
        );
    }
}
