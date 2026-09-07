package com.futsalmanager.api.dto.response;

import java.util.UUID;

public record FeedArtilheiroResponse(
        UUID usuarioId,
        String nome,
        String timeNome,
        Long gols
) {
}
