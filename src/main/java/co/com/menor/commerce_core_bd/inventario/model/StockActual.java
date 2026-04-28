package co.com.menor.commerce_core_bd.inventario.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "STOCK_ACTUAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockActual {

    @Id
    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal stock;

    @Column(name = "costo_promedio", nullable = false, precision = 14, scale = 4)
    private BigDecimal costoPromedio;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
