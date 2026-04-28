package co.com.menor.commerce_core_bd.caja.service;

import co.com.menor.comun_dto.caja.request.AbrirCajaRequest;
import co.com.menor.comun_dto.caja.request.CerrarCajaRequest;
import co.com.menor.comun_dto.caja.request.FiltroCajaRequest;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import org.springframework.data.domain.Page;

public interface CajaService {

    CajaResponse abrirCaja(AbrirCajaRequest req);

    CajaResponse cerrarCaja(CerrarCajaRequest req);

    CajaResponse obtenerPorId(Long id);

    CajaResponse obtenerCajaAbierta(Long usuarioId);

    Page<CajaResponse> buscarPaginado(FiltroCajaRequest filtro);
}
