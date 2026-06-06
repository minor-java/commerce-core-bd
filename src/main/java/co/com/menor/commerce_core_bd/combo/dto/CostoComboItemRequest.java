package co.com.menor.commerce_core_bd.combo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoComboItemRequest {
    private Long productoId;
    private BigDecimal cantidad;
}
