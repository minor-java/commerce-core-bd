package co.com.menor.commerce_core_bd.venta.service;

import co.com.menor.comun_dto.venta.request.FiltroVentaRequest;
import co.com.menor.comun_dto.venta.request.VentaRequest;
import co.com.menor.comun_dto.venta.response.VentaResponse;
import org.springframework.data.domain.Page;

public interface VentaService {

    VentaResponse crearVenta(VentaRequest req);

    VentaResponse obtenerPorId(Long id);

    Page<VentaResponse> buscarPaginado(FiltroVentaRequest filtro);
}
