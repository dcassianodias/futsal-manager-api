package com.futsalmanager.api.dto.response;

import java.util.UUID;

public record LoginResponse(

        String accessToken,
        String tokenType,
        long expiresIn,
        UUID usuarioId,
        String nome,
        String email,
        String perfil,
        UUID timeId,
        String timeNome
) {
}
