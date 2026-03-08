package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.PerfilUsuario;

import java.util.UUID;

public record UsuarioCreateRequest(
        UUID timeId,
        String nome,
        String email,
        String senha
) {
}
