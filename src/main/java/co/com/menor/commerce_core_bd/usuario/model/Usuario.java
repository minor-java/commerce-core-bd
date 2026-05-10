/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.com.menor.commerce_core_bd.usuario.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author jeffry
 */
@Table(name = "USUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contrasena;

    @Column(nullable = false)
    private Integer intentos;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = true)
    private LocalDateTime fechaActualizacion;

    @Column(name = "ultimo_login", nullable = true)
    private LocalDateTime ultimoLogin;

    @Column(length = 50)
    private String rol;
}
