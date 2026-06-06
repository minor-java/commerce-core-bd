package co.com.menor.commerce_core_bd.venta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductoResponse {
    private Long productoId;
    private Long comboId;
    private String nombre;
    private BigDecimal cantidadVendida;
    private BigDecimal totalVendido;
}
