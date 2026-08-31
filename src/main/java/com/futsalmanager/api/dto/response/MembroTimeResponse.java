package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.PerfilUsuario;

import java.util.UUID;

public record MembroTimeResponse(
        UUID timeId,
        String timeNome,
        PerfilUsuario perfil
) {
}
