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
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "PRODUCTO",
        uniqueConstraints = @javax.persistence.UniqueConstraint(
                columnNames = {"nombre", "presentacion_valor", "presentacion_unidad"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "presentacion_valor", nullable = true, length = 10)
    private BigDecimal presentacionValor;
    
    @Column(name = "presentacion_unidad", nullable = true, length = 10)
    private String presentacionUnidad;

    @Column
    private boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = true)
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "actualizado_por", nullable = true)
    private Long actualizadoPor;

    @Column(name = "precio_venta", nullable = true, precision = 14, scale = 2)
    private BigDecimal precioVenta;
}
