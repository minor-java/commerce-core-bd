package co.com.menor.commerce_core_bd.combo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoComboResponse {
    private BigDecimal costoTotal;
    private List<CostoComboItemResponse> items;
}
