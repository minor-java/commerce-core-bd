package co.com.menor.commerce_core_bd.venta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UltimaVentaResponse {
    private Long id;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;
    private Integer cantidadItems;
}
