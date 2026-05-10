package co.com.menor.commerce_core_bd.catalogo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "PRECIO_HISTORICO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PrecioHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "precio_anterior", nullable = true, precision = 14, scale = 2)
    private BigDecimal precioAnterior;

    @Column(name = "precio_nuevo", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioNuevo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
}
