package co.com.menor.commerce_core_bd.caja.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CAJA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto_inicial", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "total_ingresos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(name = "total_egresos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalEgresos = BigDecimal.ZERO;

    @Column(name = "saldo_esperado", precision = 14, scale = 2)
    private BigDecimal saldoEsperado;

    @Column(name = "monto_cierre_real", precision = 14, scale = 2)
    private BigDecimal montoCierreReal;

    @Column(name = "diferencia", precision = 14, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;
}
