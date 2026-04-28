/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.com.menor.commerce_core_bd.usuario.mapper;

import co.com.menor.commerce_core_bd.usuario.model.Usuario;
import co.com.menor.comun_dto.usuario.request.CreateUsuarioRequest;
import co.com.menor.comun_dto.usuario.request.UpdateUsuarioRequest;

import org.springframework.stereotype.Component;

/**
 *
 * @author jeffry
 */
@Component
public class UsuarioMapper {
    
    public Usuario toEntity(CreateUsuarioRequest dto) {
        
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(null);
        usuario.setUsuario(dto.getUsuario());
        usuario.setContrasena(dto.getContrasena());
        usuario.setIntentos(dto.getIntentos());
        usuario.setActivo(dto.isActivo());
        usuario.setFechaCreacion(dto.getFechaCreacion());
        usuario.setFechaActualizacion(dto.getFechaActualizacion());
        usuario.setUltimoLogin(dto.getUltimoLogin());
        usuario.setRol(dto.getRol());

        return usuario;
    }
    
    public void updateEntityFromRequest(UpdateUsuarioRequest req, Usuario usuario) {
        
        if (req == null || usuario == null) return;

        if (req.getContrasena() != null) {
            usuario.setContrasena(req.getContrasena());
        }
        if (req.getIntentos() != null) {
            usuario.setIntentos(req.getIntentos());
        }
        if (req.getActivo() != null) {
            usuario.setActivo(req.getActivo());
        }
        if (req.getUltimoLogin() != null) {
            usuario.setUltimoLogin(req.getUltimoLogin());
        }
        if (req.getFechaActualizacion() != null) {
            usuario.setFechaActualizacion(req.getFechaActualizacion());
        }
        if (req.getRol() != null) {
            usuario.setRol(req.getRol());
        }
    }
}
