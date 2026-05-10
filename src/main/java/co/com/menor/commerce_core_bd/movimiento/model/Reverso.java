package co.com.menor.commerce_core_bd.movimiento.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "REVERSO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reverso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movimiento_id", nullable = false)
    private Long movimientoId;

    @Column(name = "cantidad_reversada", nullable = false, precision = 14, scale = 2)
    private BigDecimal cantidadReversada;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;
}
