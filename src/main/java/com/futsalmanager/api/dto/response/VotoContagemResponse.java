package com.futsalmanager.api.dto.response;

import java.util.UUID;

public record VotoContagemResponse(
        UUID usuarioId,
        String nome,
        Long votos
) {
}
