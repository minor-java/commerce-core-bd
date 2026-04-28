package co.com.menor.commerce_core_bd.caja.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMIENTO_CAJA",
        indexes = {
            @Index(name = "idx_movcaja_caja", columnList = "caja_id, fecha_creacion"),
            @Index(name = "idx_movcaja_referencia", columnList = "referencia_tipo, referencia_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caja_id", nullable = false)
    private Long cajaId;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "referencia_tipo", nullable = false, length = 20)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;
}
