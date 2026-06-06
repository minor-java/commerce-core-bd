package co.com.menor.commerce_core_bd.combo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoComboRequest {
    private List<CostoComboItemRequest> items;
}
