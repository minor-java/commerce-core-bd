package co.com.menor.commerce_core_bd.venta.service;

import co.com.menor.commerce_core_bd.venta.dto.TopProductoResponse;
import co.com.menor.commerce_core_bd.venta.dto.UltimaVentaResponse;
import co.com.menor.comun_dto.venta.request.FiltroVentaRequest;
import co.com.menor.comun_dto.venta.request.VentaRequest;
import co.com.menor.comun_dto.venta.response.VentaResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface VentaService {

    VentaResponse crearVenta(VentaRequest req);

    VentaResponse obtenerPorId(Long id);

    Page<VentaResponse> buscarPaginado(FiltroVentaRequest filtro);

    BigDecimal getTotalVendidoMes();

    Long getCantidadVentasMes();

    BigDecimal getTotalVendidoHoy();

    List<UltimaVentaResponse> getUltimasVentas();

    List<TopProductoResponse> getTopProductosMes();
}
