package com.futsalmanager.api.dto.response;

import java.util.List;
import java.util.UUID;

public record LoginResponse(

        String accessToken,
        String tokenType,
        long expiresIn,
        UUID usuarioId,
        String nome,
        String email,
        List<MembroTimeResponse> times
) {
}
