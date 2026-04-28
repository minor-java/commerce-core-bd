package co.com.menor.commerce_core_bd.catalogo.model;

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

@Table(name = "CODIGO_BARRA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CodigoBarra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = true)
    private String tipo;

    @Column
    private boolean principal;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = true)
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @Column(name = "actualizado_por", nullable = true)
    private Long actualizadoPor;
}
