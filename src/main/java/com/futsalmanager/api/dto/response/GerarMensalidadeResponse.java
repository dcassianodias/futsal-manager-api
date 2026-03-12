package com.futsalmanager.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record GerarMensalidadeResponse(
        UUID timeId,
        LocalDate mesReferencia,
        int totalAtletas,
        int totalGerados,
        int totalJaExistentes
) {
}
