package com.futsalmanager.security.service;

import com.futsalmanager.domain.entities.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticatedUserProvider {

    public Usuario getUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!(principal instanceof Usuario usuario)){
            throw new IllegalStateException("Usuário não autenticado");
        }
        return usuario;
    }

    public UUID getTimeId() {
        return getUsuarioAutenticado().getTime().getId();
    }

    public void validarAcessoAoTime(UUID timeId) {
        if(!getTimeId().equals(timeId)){
            throw new AccessDeniedException("Acesso negado ao time");
        }
    }
}
