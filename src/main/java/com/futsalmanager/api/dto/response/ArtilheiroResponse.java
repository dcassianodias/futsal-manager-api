package com.futsalmanager.api.dto.response;

import java.util.UUID;

public record ArtilheiroResponse(
        UUID usuarioId,
        String nome,
        Long gols
) {
}
