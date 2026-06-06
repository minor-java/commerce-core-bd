package co.com.menor.commerce_core_bd.combo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoComboItemResponse {
    private Long productoId;
    private String nombreProducto;
    private BigDecimal costoUnitario;
    private BigDecimal cantidad;
    private BigDecimal costoItem;
}
