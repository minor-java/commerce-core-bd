package co.com.menor.commerce_core_bd.compra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.com.menor.commerce_core_bd.compra.mapper.CompraDetalleMapper;
import co.com.menor.commerce_core_bd.compra.model.CompraDetalle;
import co.com.menor.commerce_core_bd.compra.repository.CompraDetalleRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.compra.response.CompraDetalleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraDetalleServiceImpl implements CompreDetalleService {

    private final CompraDetalleRepository compraDetalleRepository;

    private final CompraDetalleMapper compraDetalleMapper;

    @Override
    public CompraDetalleResponse obtenerCompraDetallePorId(Long detalleId) {
        
        try {
            
            return compraDetalleRepository.findById(detalleId)
            .map(compraDetalleMapper::toDetalleResponse)
            .orElse(null);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CompreDetalleService obtenerCompraDetallePorId"
            );
        }
    }

    @Override
    public List<CompraDetalleResponse> obtenerDetallesPorCompraId(Long compraId) {

        try {

            List<CompraDetalle> detalles =
            compraDetalleRepository.findByCompraId(compraId);

            return compraDetalleMapper.toDetalleResponseList(detalles);

        } catch (Exception e) {

            throw new MinorExcepcion(
                "ERROR",
                "CompraDetalleService obtenerDetallesPorCompraId"
            );
        }
    }

    @Override
    public List<CompraDetalle> guardarDetalles(List<CompraDetalle> detalles) {

        try {
            
            return compraDetalleRepository.saveAll(detalles);
        } catch (Exception e) {
            throw new MinorExcepcion(
                "ERROR",
                "CompreDetalleService guardarDetalles"
            );
        }
    }
}
