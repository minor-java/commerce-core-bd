package co.com.menor.commerce_core_bd.combo.service;

import co.com.menor.commerce_core_bd.catalogo.model.PrecioHistorico;
import co.com.menor.commerce_core_bd.catalogo.repository.PrecioHistoricoRepository;
import co.com.menor.commerce_core_bd.catalogo.repository.ProductoRepository;
import co.com.menor.commerce_core_bd.catalogo.service.CodigoBarrasService;
import co.com.menor.commerce_core_bd.combo.dto.CostoComboItemRequest;
import co.com.menor.commerce_core_bd.combo.dto.CostoComboItemResponse;
import co.com.menor.commerce_core_bd.combo.dto.CostoComboRequest;
import co.com.menor.commerce_core_bd.combo.dto.CostoComboResponse;
import co.com.menor.commerce_core_bd.combo.mapper.ComboDetalleMapper;
import co.com.menor.commerce_core_bd.combo.mapper.ComboMapper;
import co.com.menor.commerce_core_bd.combo.model.Combo;
import co.com.menor.commerce_core_bd.combo.model.ComboDetalle;
import co.com.menor.commerce_core_bd.combo.repository.ComboDetalleRepository;
import co.com.menor.commerce_core_bd.combo.repository.ComboRepository;
import co.com.menor.commerce_core_bd.combo.repository.ComboSpecification;
import co.com.menor.commerce_core_bd.movimiento.service.StockActualService;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import co.com.menor.comun_dto.combo.request.CreateComboRequest;
import co.com.menor.comun_dto.combo.request.CreateComboDetalleRequest;
import co.com.menor.comun_dto.combo.request.UpdateComboRequest;
import co.com.menor.comun_dto.utils.CodigoBarrasConstants;
import co.com.menor.comun_dto.combo.request.FiltroComboRequest;
import co.com.menor.comun_dto.combo.response.ComboResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final ComboDetalleService comboDetalleService;
    private final ComboDetalleRepository comboDetalleRepository;
    private final ComboMapper comboMapper;
    private final ComboDetalleMapper comboDetalleMapper;
    private final StockActualService stockActualService;
    private final ProductoRepository productoRepository;
    private final CodigoBarrasService codigoBarrasService;
    private final PrecioHistoricoRepository precioHistoricoRepository;

    @Override
    @Transactional(readOnly = true)
    public ComboResponse obtenerPorId(Long id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new MinorExcepcion("COMBO_NO_ENCONTRADO", "No existe un combo con id: " + id));
        List<ComboDetalle> detalles = comboDetalleService.buscarPorComboId(id);
        List<String> codigos = codigoBarrasService.findByComboId(id).stream()
                .map(c -> c.getCodigo())
                .collect(Collectors.toList());
        return comboMapper.toResponse(combo, comboDetalleMapper.toResponseList(detalles), codigos);
    }

    @Override
    @Transactional
    public ComboResponse crearCombo(CreateComboRequest req) {
        try {
            validarStockParaDetalles(req.getDetalles(), req.getCantidadDisponible());

            Combo combo = comboMapper.toEntity(req);
            Combo comboGuardado = comboRepository.save(combo);
            log.info("crearCombo: combo guardado id={}", comboGuardado.getId());

            List<ComboDetalle> detalles = req.getDetalles().stream()
                    .map(d -> comboDetalleMapper.toEntity(d, comboGuardado.getId()))
                    .collect(Collectors.toList());

            List<ComboDetalle> detallesGuardados = comboDetalleService.guardarDetalles(detalles);
            log.info("crearCombo: {} detalles guardados para comboId={}", detallesGuardados.size(), comboGuardado.getId());

            List<String> codigosGuardados = guardarCodigosBarras(req, comboGuardado.getId());

            if (comboGuardado.getPrecioBase() != null) {
                precioHistoricoRepository.save(PrecioHistorico.builder()
                        .comboId(comboGuardado.getId())
                        .precioAnterior(null)
                        .precioNuevo(comboGuardado.getPrecioBase())
                        .fechaCreacion(comboGuardado.getFechaCreacion())
                        .usuarioId(comboGuardado.getUsuarioId())
                        .build());
            }

            return comboMapper.toResponse(comboGuardado, comboDetalleMapper.toResponseList(detallesGuardados), codigosGuardados);
        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboService crearCombo");
        }
    }

    @Override
    @Transactional
    public ComboResponse actualizarCombo(Long id, UpdateComboRequest req) {
        try {
            Combo combo = comboRepository.findById(id)
                    .orElseThrow(() -> new MinorExcepcion("COMBO_NO_ENCONTRADO", "No existe un combo con id: " + id));

            if (req.getCantidadDisponible() != null
                    && req.getCantidadDisponible() > combo.getCantidadDisponible()) {
                int delta = req.getCantidadDisponible() - combo.getCantidadDisponible();
                validarStockParaAumento(id, delta);
            }

            java.math.BigDecimal precioAnterior = combo.getPrecioBase();

            if (req.getNombre() != null) combo.setNombre(req.getNombre());
            if (req.getDescripcion() != null) combo.setDescripcion(req.getDescripcion());
            if (req.getPrecioBase() != null) combo.setPrecioBase(req.getPrecioBase());
            if (req.getActivo() != null) combo.setActivo(req.getActivo());
            if (req.getCantidadDisponible() != null) combo.setCantidadDisponible(req.getCantidadDisponible());

            Combo actualizado = comboRepository.save(combo);

            java.math.BigDecimal precioNuevo = actualizado.getPrecioBase();
            boolean precioChanged = precioNuevo != null
                    && (precioAnterior == null || precioAnterior.compareTo(precioNuevo) != 0);
            if (precioChanged) {
                precioHistoricoRepository.save(PrecioHistorico.builder()
                        .comboId(actualizado.getId())
                        .precioAnterior(precioAnterior)
                        .precioNuevo(precioNuevo)
                        .fechaCreacion(java.time.LocalDateTime.now())
                        .usuarioId(req.getUsuarioId() != null ? req.getUsuarioId() : actualizado.getUsuarioId())
                        .build());
            }

            List<ComboDetalle> detalles = comboDetalleService.buscarPorComboId(id);

            if (req.getCodigosBarras() != null) {
                java.util.List<co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra> actuales =
                        codigoBarrasService.findByComboId(id);

                java.util.Set<String> codigosNuevos = req.getCodigosBarras().stream()
                        .filter(c -> c != null && !c.trim().isEmpty())
                        .map(String::trim)
                        .collect(java.util.stream.Collectors.toSet());

                java.util.Set<String> codigosActuales = actuales.stream()
                        .map(c -> c.getCodigo())
                        .collect(java.util.stream.Collectors.toSet());

                // Eliminar solo los que ya no están en el nuevo listado
                java.util.List<Long> idsAEliminar = actuales.stream()
                        .filter(c -> !codigosNuevos.contains(c.getCodigo()))
                        .map(c -> c.getId())
                        .collect(Collectors.toList());
                if (!idsAEliminar.isEmpty()) {
                    co.com.menor.comun_dto.codigo_barras.request.EliminarCodigosBarrasRequest elimReq =
                            new co.com.menor.comun_dto.codigo_barras.request.EliminarCodigosBarrasRequest();
                    elimReq.setIds(idsAEliminar);
                    codigoBarrasService.deleteByIds(elimReq);
                }

                // Agregar solo los que son verdaderamente nuevos
                for (String codigo : codigosNuevos) {
                    if (!codigosActuales.contains(codigo)) {
                        CreateCondigoBarrasRequest cbReq = new CreateCondigoBarrasRequest();
                        cbReq.setCodigo(codigo);
                        cbReq.setComboId(id);
                        cbReq.setProductoId(null);
                        cbReq.setTipo(CodigoBarrasConstants.TIPO_EAN_13);
                        cbReq.setFechaCreacion(java.time.LocalDateTime.now());
                        cbReq.setUsuarioId(req.getUsuarioId());
                        codigoBarrasService.saveCodigoBarras(cbReq);
                    }
                }
            }

            List<String> codigos = codigoBarrasService.findByComboId(id).stream()
                    .map(c -> c.getCodigo()).collect(Collectors.toList());
            return comboMapper.toResponse(actualizado, comboDetalleMapper.toResponseList(detalles), codigos);
        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboService actualizarCombo");
        }
    }

    private void validarStockParaAumento(Long comboId, int delta) {
        List<ComboDetalle> detalles = comboDetalleService.buscarPorComboId(comboId);
        for (ComboDetalle detalle : detalles) {
            Long productoId = detalle.getProductoId();
            BigDecimal stockActual = stockActualService.buscarPorProductoId(productoId)
                    .map(s -> s.getStock()).orElse(BigDecimal.ZERO);
            BigDecimal comprometido = comboDetalleRepository.sumCantidadByProductoIdAndComboActivo(productoId);
            BigDecimal disponible = stockActual.subtract(comprometido);
            BigDecimal necesario = detalle.getCantidad().multiply(BigDecimal.valueOf(delta));
            if (disponible.compareTo(necesario) < 0) {
                String nombre = productoRepository.findById(productoId)
                        .map(p -> p.getNombre()).orElse("id=" + productoId);
                throw new MinorExcepcion("STOCK_INSUFICIENTE_COMBO",
                        "Stock insuficiente para producto " + nombre );
            }
        }
    }

    private List<String> guardarCodigosBarras(CreateComboRequest req, Long comboId) {
        if (req.getCodigosBarras() == null || req.getCodigosBarras().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> guardados = new ArrayList<>();
        for (String codigo : req.getCodigosBarras()) {
            if (codigo == null || codigo.trim().isEmpty()) continue;
            CreateCondigoBarrasRequest cbReq = new CreateCondigoBarrasRequest();
            cbReq.setCodigo(codigo.trim());
            cbReq.setComboId(comboId);
            cbReq.setProductoId(null);
            cbReq.setTipo(CodigoBarrasConstants.TIPO_EAN_13);
            cbReq.setFechaCreacion(java.time.LocalDateTime.now());
            cbReq.setUsuarioId(req.getUsuarioId());
            codigoBarrasService.saveCodigoBarras(cbReq);
            guardados.add(codigo.trim());
        }
        return guardados;
    }

    private void validarStockParaDetalles(List<CreateComboDetalleRequest> detalles, Integer cantidadCombo) {
        for (CreateComboDetalleRequest detalle : detalles) {
            Long productoId = detalle.getProductoId();
            BigDecimal cantidadNecesaria = detalle.getCantidad().multiply(BigDecimal.valueOf(cantidadCombo));

            BigDecimal stockActual = stockActualService.buscarPorProductoId(productoId)
                    .map(s -> s.getStock())
                    .orElse(BigDecimal.ZERO);

            BigDecimal yaComprometido = comboDetalleRepository.sumCantidadByProductoIdAndComboActivo(productoId);
            BigDecimal disponible = stockActual.subtract(yaComprometido);

            if (disponible.compareTo(cantidadNecesaria) < 0) {
                String nombre = productoRepository.findById(productoId)
                        .map(p -> p.getNombre())
                        .orElse("id=" + productoId);
                throw new MinorExcepcion(
                        "STOCK_INSUFICIENTE_COMBO",
                        "Stock insuficiente para producto \"" + nombre + "\"");
            }
        }
    }

    @Override
    public Page<ComboResponse> buscarPaginado(FiltroComboRequest filtro) {
        try {
            PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
            return comboRepository.findAll(ComboSpecification.buildFrom(filtro), pageable)
                    .map(combo -> {
                        List<String> codigos = codigoBarrasService.findByComboId(combo.getId()).stream()
                                .map(c -> c.getCodigo())
                                .collect(Collectors.toList());
                        return comboMapper.toResponse(combo, Collections.emptyList(), codigos);
                    });
        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboService buscarPaginado");
        }
    }

    @Override
    @Transactional
    public void eliminarCombo(Long id) {
        try {
            codigoBarrasService.deleteByComboId(id);
            comboDetalleService.eliminarPorComboId(id);
            comboRepository.deleteById(id);
            log.info("eliminarCombo: combo id={} eliminado con sus detalles y códigos", id);
        } catch (MinorExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new MinorExcepcion("ERROR", "ComboService eliminarCombo");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getStockComprometido(Long productoId) {
        return comboDetalleRepository.sumCantidadByProductoIdAndComboActivo(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Long, BigDecimal> getStockComprometidoBatch(java.util.List<Long> productoIds) {
        java.util.Map<Long, BigDecimal> result = new java.util.HashMap<>();
        if (productoIds == null || productoIds.isEmpty()) return result;
        List<Object[]> rows = comboDetalleRepository.sumCantidadGroupByProductoIdForActiveCombos(productoIds);
        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            BigDecimal sum = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            result.put(id, sum);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CostoComboResponse calcularCosto(CostoComboRequest request) {
        List<CostoComboItemResponse> items = new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;

        for (CostoComboItemRequest item : request.getItems()) {
            BigDecimal costoUnitario = stockActualService.buscarPorProductoId(item.getProductoId())
                    .map(s -> s.getCostoPromedio())
                    .orElse(BigDecimal.ZERO);

            BigDecimal costoItem = costoUnitario.multiply(item.getCantidad());
            costoTotal = costoTotal.add(costoItem);

            String nombre = productoRepository.findById(item.getProductoId())
                    .map(p -> p.getNombre())
                    .orElse("Producto id=" + item.getProductoId());

            items.add(CostoComboItemResponse.builder()
                    .productoId(item.getProductoId())
                    .nombreProducto(nombre)
                    .costoUnitario(costoUnitario)
                    .cantidad(item.getCantidad())
                    .costoItem(costoItem)
                    .build());
        }

        return CostoComboResponse.builder()
                .costoTotal(costoTotal)
                .items(items)
                .build();
    }
}
