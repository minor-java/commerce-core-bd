package co.com.menor.commerce_core_bd.caja.service;

import co.com.menor.comun_dto.caja.request.AbrirCajaRequest;
import co.com.menor.comun_dto.caja.request.CerrarCajaRequest;
import co.com.menor.comun_dto.caja.request.FiltroCajaRequest;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import co.com.menor.comun_dto.caja.response.MovimientoCajaResponse;

import org.springframework.data.domain.Page;
import java.util.List;

public interface CajaService {

    CajaResponse abrirCaja(AbrirCajaRequest req);

    CajaResponse cerrarCaja(CerrarCajaRequest req);

    CajaResponse obtenerPorCajaId(Long id);

    Boolean existeCaja(Long usuarioId);

    Boolean tieneCajaActiva(Long usuarioId);

    Page<CajaResponse> buscarPaginado(FiltroCajaRequest filtro);

    CajaResponse obtenerPorUsuarioIdPorEstado(Long usuarioId);

    List<MovimientoCajaResponse> consultarMovimientosPorCajaId(Long cajaId);

}
