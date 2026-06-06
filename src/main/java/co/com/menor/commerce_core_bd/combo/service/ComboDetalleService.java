package co.com.menor.commerce_core_bd.combo.service;

import co.com.menor.commerce_core_bd.combo.model.ComboDetalle;

import java.util.List;

public interface ComboDetalleService {

    List<ComboDetalle> guardarDetalles(List<ComboDetalle> detalles);

    List<ComboDetalle> buscarPorComboId(Long comboId);

    void eliminarPorComboId(Long comboId);
}
