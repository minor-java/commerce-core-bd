package co.com.menor.commerce_core_bd.venta.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.repository.VentaDetalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaDetalleServiceImpl implements VentaDetalleService {

    private final VentaDetalleRepository ventaDetalleRepository;

    @Override
    public List<VentaDetalle> guardarTodo(List<VentaDetalle> detalles) {

        return ventaDetalleRepository.saveAll(detalles);
    }

    @Override
    public List<VentaDetalle> buscarPorVentaId(Long id) {

        return ventaDetalleRepository.findByVentaId(id);
    }

    @Override
    public Optional<VentaDetalle> buscarPorId(Long id) {

        return ventaDetalleRepository.findById(id);
    }
}
