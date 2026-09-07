package com.futsalmanager.api.dto.response;

import java.util.List;
import java.util.UUID;

public record ResultadoVotacaoResponse(
        List<VotoContagemResponse> resultado,
        UUID meuVoto
) {
}
