package co.com.menor.commerce_core_bd.venta.service;

import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.commerce_core_bd.catalogo.model.Producto;
import co.com.menor.commerce_core_bd.catalogo.repository.ProductoRepository;
import co.com.menor.commerce_core_bd.movimiento.model.MovimientoInventario;
import co.com.menor.commerce_core_bd.movimiento.service.MovimientoService;
import co.com.menor.commerce_core_bd.movimiento.service.StockActualService;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.commerce_core_bd.venta.dto.TopProductoResponse;
import co.com.menor.commerce_core_bd.venta.dto.UltimaVentaResponse;
import co.com.menor.commerce_core_bd.venta.mapper.VentaMapper;
import co.com.menor.commerce_core_bd.venta.mapper.VentaResponseMapper;
import co.com.menor.commerce_core_bd.venta.model.Venta;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.repository.VentaRepository;
import co.com.menor.comun_dto.utils.CajaConstants;
import co.com.menor.comun_dto.utils.MovimientoInventarioConstants;
import co.com.menor.comun_dto.venta.request.FiltroVentaRequest;
import co.com.menor.comun_dto.venta.request.VentaRequest;
import co.com.menor.comun_dto.venta.response.VentaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleService ventaDetalleService;

    private final MovimientoService movimientoService;

    private final VentaMapper ventaMapper;
    private final VentaResponseMapper ventaResponseMapper;

    private final StockActualService stockActualService;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public VentaResponse crearVenta(VentaRequest req) {
        
        Venta venta = ventaMapper.toEntity(req);
        Venta ventaGuardada = ventaRepository.save(venta);
        log.info("crearVenta: venta guardada id={}", ventaGuardada.getId());

        List<VentaDetalle> detalles = req.getDetalles()
        .stream()
        .map(d -> ventaMapper.toDetalleEntity(d, ventaGuardada.getId()))
        .collect(Collectors.toList());

        List<VentaDetalle> detallesGuardados = ventaDetalleService.guardarTodo(detalles);

        detallesGuardados.forEach(detalle -> {
            
            BigDecimal costoUnitario = 
            stockActualService.buscarPorProductoId(detalle.getProductoId())
            .map(s -> s.getCostoPromedio())
                .orElse(BigDecimal.ZERO);

            MovimientoInventario movimiento = new MovimientoInventario();

            movimiento.setProductoId(detalle.getProductoId());
            movimiento.setTipo(MovimientoInventarioConstants.TIPO_SALIDA);
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setCostoUnitario(costoUnitario);
            movimiento.setCostoTotal(detalle.getCantidad().multiply(costoUnitario));
            movimiento.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_VENTA_DETALLE);
            movimiento.setReferenciaId(detalle.getId());
            movimiento.setFechaCreacion(LocalDateTime.now());
            movimiento.setUsuarioId(req.getUsuarioId());

            movimientoService.guardarMovimiento(movimiento);
            stockActualService.actualizarStock(movimiento);
        });

        BigDecimal total = detallesGuardados.stream()
        .map(VentaDetalle::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        ventaGuardada.setTotal(total);
        ventaRepository.save(ventaGuardada);

        MovimientoCaja movCaja = new MovimientoCaja();
        movCaja.setCajaId(req.getCajaId());
        movCaja.setTipo(CajaConstants.TIPO_INGRESO);
        movCaja.setMetodoPago(req.getMetodoPago());
        movCaja.setMonto(total);
        movCaja.setReferenciaTipo(MovimientoInventarioConstants.REFERENCIA_VENTA);
        movCaja.setReferenciaId(ventaGuardada.getId());
        movCaja.setFechaCreacion(LocalDateTime.now());
        movCaja.setUsuarioId(req.getUsuarioId());

        movimientoService.guardarMovimientoCaja(movCaja);
        
        log.info("crearVenta: movimiento caja registrado cajaId={} monto={}", req.getCajaId(), total);

        return ventaResponseMapper.toResponse(ventaGuardada, detallesGuardados);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new MinorExcepcion("VENTA_NO_ENCONTRADA",
                        "No existe una venta con id: " + id));
                        
        List<VentaDetalle> detalles = ventaDetalleService.buscarPorVentaId(id);
        return ventaResponseMapper.toResponse(venta, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPaginado(FiltroVentaRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Specification<Venta> spec = buildSpec(filtro);
        return ventaRepository.findAll(spec, pageable)
                .map(v -> ventaResponseMapper.toResponse(v, ventaDetalleService.buscarPorVentaId(v.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalVendidoMes() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now();
        BigDecimal total = ventaRepository.sumTotalByFechaCreacionBetween(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCantidadVentasMes() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now();
        return ventaRepository.countByFechaCreacionBetween(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalVendidoHoy() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        BigDecimal total = ventaRepository.sumTotalByFechaCreacionBetween(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UltimaVentaResponse> getUltimasVentas() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now();
        return ventaRepository.findTop5ByFechaCreacionBetweenOrderByIdDesc(inicio, fin)
                .stream()
                .map(v -> new UltimaVentaResponse(
                        v.getId(),
                        v.getTotal(),
                        v.getFechaCreacion(),
                        ventaDetalleService.buscarPorVentaId(v.getId()).size()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductoResponse> getTopProductosMes() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now();

        List<Long> ventaIds = ventaRepository.findByFechaCreacionBetween(inicio, fin)
                .stream().map(Venta::getId).collect(Collectors.toList());

        if (ventaIds.isEmpty()) return Collections.emptyList();

        Map<Long, BigDecimal[]> mapa = new HashMap<>();
        for (VentaDetalle d : ventaDetalleService.buscarPorVentaIds(ventaIds)) {
            BigDecimal[] acum = mapa.computeIfAbsent(d.getProductoId(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acum[0] = acum[0].add(d.getCantidad());
            acum[1] = acum[1].add(d.getSubtotal());
        }

        return mapa.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0].compareTo(a.getValue()[0]))
                .limit(3)
                .map(e -> {
                    String nombre = productoRepository.findById(e.getKey())
                            .map(this::buildNombreProducto)
                            .orElse("Producto #" + e.getKey());
                    return new TopProductoResponse(e.getKey(), nombre, e.getValue()[0], e.getValue()[1]);
                })
                .collect(Collectors.toList());
    }

    private String buildNombreProducto(Producto p) {
        StringBuilder sb = new StringBuilder(p.getNombre());
        if (p.getPresentacionValor() != null) {
            sb.append(" ").append(p.getPresentacionValor().stripTrailingZeros().toPlainString());
        }
        if (p.getPresentacionUnidad() != null) {
            sb.append(p.getPresentacionUnidad());
        }
        return sb.toString();
    }

    private Specification<Venta> buildSpec(FiltroVentaRequest filtro) {
        return (root, query, cb) -> {
            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (filtro.getCajaId() != null) {
                predicates.add(cb.equal(root.get("cajaId"), filtro.getCajaId()));
            }
            if (filtro.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filtro.getId()));
            }
            if (filtro.getUsuarioId() != null) {
                predicates.add(cb.equal(root.get("usuarioId"), filtro.getUsuarioId()));
            }
            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaDesde()));
            }
            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaHasta()));
            }
            if (filtro.getTotal() != null) {
                predicates.add(cb.equal(root.get("total"), filtro.getTotal()));
            }
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }
}
