package com.futsalmanager.security.service;

import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticatedUserProvider {

    private final UsuarioTimeRepository usuarioTimeRepository;

    public AuthenticatedUserProvider(UsuarioTimeRepository usuarioTimeRepository) {
        this.usuarioTimeRepository = usuarioTimeRepository;
    }

    public Usuario getUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!(principal instanceof Usuario usuario)){
            throw new IllegalStateException("Usuário não autenticado");
        }
        return usuario;
    }

    /**
     * Valida que o usuário autenticado tem vínculo ativo (qualquer perfil) com o time informado.
     */
    public void validarMembro(UUID timeId) {
        UUID usuarioId = getUsuarioAutenticado().getId();
        if (!usuarioTimeRepository.existsByUsuarioIdAndTimeIdAndAtivoTrue(usuarioId, timeId)) {
            throw new AccessDeniedException("Acesso negado ao time");
        }
    }

    /**
     * Valida que o usuário autenticado é ADMIN (com vínculo ativo) do time informado.
     */
    public void validarAdminDoTime(UUID timeId) {
        UUID usuarioId = getUsuarioAutenticado().getId();
        if (!usuarioTimeRepository.existsByUsuarioIdAndTimeIdAndPerfilAndAtivoTrue(
                usuarioId, timeId, PerfilUsuario.ADMIN)) {
            throw new AccessDeniedException("Acesso negado: requer perfil ADMIN neste time");
        }
    }
}
