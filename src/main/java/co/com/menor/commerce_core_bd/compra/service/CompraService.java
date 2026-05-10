package co.com.menor.commerce_core_bd.compra.service;

import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import org.springframework.data.domain.Page;

public interface CompraService {

    CompraResponse crearCompra(CompraRequest req);

    CompraResponse obtenerCompraPorId(Long id);

    Page<CompraResponse> buscarComprasPaginado(FiltroCompraRequest filtro);
}
