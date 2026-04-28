package co.com.menor.commerce_core_bd.inventario.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMIENTO_INVENTARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 14, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "referencia_tipo", nullable = false, length = 20)
    private String referenciaTipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @Column(name = "movimiento_origen_id")
    private Long movimientoOrigenId;
}
