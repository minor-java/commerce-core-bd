package co.com.menor.commerce_core_bd.inventario.service;

import co.com.menor.comun_dto.reverso.request.ReversoRequest;
import co.com.menor.comun_dto.reverso.response.ReversoResponse;

public interface ReversoService {

    ReversoResponse crearReverso(ReversoRequest req);
}
