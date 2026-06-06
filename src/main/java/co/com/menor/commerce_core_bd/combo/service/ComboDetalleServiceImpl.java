package co.com.menor.commerce_core_bd.combo.service;

import co.com.menor.commerce_core_bd.combo.model.ComboDetalle;
import co.com.menor.commerce_core_bd.combo.repository.ComboDetalleRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComboDetalleServiceImpl implements ComboDetalleService {

    private final ComboDetalleRepository comboDetalleRepository;

    @Override
    public List<ComboDetalle> guardarDetalles(List<ComboDetalle> detalles) {
        try {
            return comboDetalleRepository.saveAll(detalles);
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboDetalleService guardarDetalles");
        }
    }

    @Override
    public List<ComboDetalle> buscarPorComboId(Long comboId) {
        try {
            return comboDetalleRepository.findByComboId(comboId);
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboDetalleService buscarPorComboId");
        }
    }

    @Override
    public void eliminarPorComboId(Long comboId) {
        try {
            comboDetalleRepository.deleteByComboId(comboId);
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboDetalleService eliminarPorComboId");
        }
    }
}
