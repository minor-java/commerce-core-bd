package co.com.menor.commerce_core_bd.combo.service;

import co.com.menor.commerce_core_bd.combo.dto.CostoComboRequest;
import co.com.menor.commerce_core_bd.combo.dto.CostoComboResponse;
import co.com.menor.comun_dto.combo.request.CreateComboRequest;
import co.com.menor.comun_dto.combo.request.FiltroComboRequest;
import co.com.menor.comun_dto.combo.request.UpdateComboRequest;
import co.com.menor.comun_dto.combo.response.ComboResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface ComboService {

    ComboResponse crearCombo(CreateComboRequest req);

    ComboResponse actualizarCombo(Long id, UpdateComboRequest req);

    ComboResponse obtenerPorId(Long id);

    void eliminarCombo(Long id);

    Page<ComboResponse> buscarPaginado(FiltroComboRequest filtro);

    BigDecimal getStockComprometido(Long productoId);

    java.util.Map<Long, java.math.BigDecimal> getStockComprometidoBatch(java.util.List<Long> productoIds);

    CostoComboResponse calcularCosto(CostoComboRequest request);
}
