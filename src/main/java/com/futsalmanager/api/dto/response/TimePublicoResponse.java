package com.futsalmanager.api.dto.response;

import java.util.List;
import java.util.UUID;

public record TimePublicoResponse(
        UUID id,
        String nome,
        JogoResponse proximoJogo,
        List<JogoResponse> ultimosResultados
) {
}
