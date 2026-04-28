package co.com.menor.commerce_core_bd.compra.service;

import co.com.menor.comun_dto.compra.request.CompraRequest;
import co.com.menor.comun_dto.compra.request.FiltroCompraRequest;
import co.com.menor.comun_dto.compra.response.CompraDetalleByIdResponse;
import co.com.menor.comun_dto.compra.response.CompraResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CompraService {

    CompraResponse crearCompra(CompraRequest req);

    List<CompraResponse> obtenerTodas();

    List<CompraResponse> obtenerTodasConDetalles();

    Page<CompraResponse> buscarDetalladas(FiltroCompraRequest filtro);

    CompraResponse obtenerPorId(Long id);

    CompraDetalleByIdResponse obtenerDetallePorId(Long id);
}
