package co.com.menor.commerce_core_bd.venta.controller;

import co.com.menor.commerce_core_bd.venta.dto.TopProductoResponse;
import co.com.menor.commerce_core_bd.venta.dto.UltimaVentaResponse;
import co.com.menor.commerce_core_bd.venta.mapper.VentaResponseMapper;
import co.com.menor.commerce_core_bd.venta.model.VentaDetalle;
import co.com.menor.commerce_core_bd.venta.service.VentaDetalleService;
import co.com.menor.commerce_core_bd.venta.service.VentaService;
import co.com.menor.comun_dto.paginacion.PaginadoResponse;
import co.com.menor.comun_dto.venta.request.FiltroVentaRequest;
import co.com.menor.comun_dto.venta.request.VentaRequest;
import co.com.menor.comun_dto.venta.response.VentaDetalleResponse;
import co.com.menor.comun_dto.venta.response.VentaResponse;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/venta")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final VentaDetalleService ventaDetalleService;
    private final VentaResponseMapper ventaResponseMapper;

    @PostMapping
    public ResponseEntity<VentaResponse> crearVenta(@RequestBody VentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crearVenta(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<VentaDetalleResponse> obtenerDetallePorId(@PathVariable Long id) {
        VentaDetalle detalle = ventaDetalleService.buscarPorId(id)
            .orElseThrow(() -> new MinorExcepcion("VENTA_DETALLE_NO_ENCONTRADO", "VentaDetalle no encontrado: " + id));
        return ResponseEntity.ok(ventaResponseMapper.toDetalleResponse(detalle));
    }

    @PostMapping("/paginado")
    public ResponseEntity<PaginadoResponse<VentaResponse>> buscarPaginado(
            @RequestBody FiltroVentaRequest filtro) {
        Page<VentaResponse> page = ventaService.buscarPaginado(filtro);
        PaginadoResponse<VentaResponse> respuesta = new PaginadoResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/total-vendido-mes")
    public ResponseEntity<BigDecimal> totalVendidoMes() {
        return ResponseEntity.ok(ventaService.getTotalVendidoMes());
    }

    @GetMapping("/cantidad-ventas-mes")
    public ResponseEntity<Long> cantidadVentasMes() {
        return ResponseEntity.ok(ventaService.getCantidadVentasMes());
    }

    @GetMapping("/total-vendido-hoy")
    public ResponseEntity<BigDecimal> totalVendidoHoy() {
        return ResponseEntity.ok(ventaService.getTotalVendidoHoy());
    }

    @GetMapping("/ultimas-ventas")
    public ResponseEntity<List<UltimaVentaResponse>> ultimasVentas() {
        return ResponseEntity.ok(ventaService.getUltimasVentas());
    }

    @GetMapping("/top-productos-mes")
    public ResponseEntity<List<TopProductoResponse>> topProductosMes() {
        return ResponseEntity.ok(ventaService.getTopProductosMes());
    }
}
