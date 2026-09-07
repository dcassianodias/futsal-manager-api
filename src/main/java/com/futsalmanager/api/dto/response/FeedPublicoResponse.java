package com.futsalmanager.api.dto.response;

import java.util.List;

public record FeedPublicoResponse(
        List<FeedJogoResponse> jogos,
        List<FeedArtilheiroResponse> artilheiros
) {
}
