package com.futsalmanager.api.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record GerarMensalidadeRequest(
        UUID timeId,
        LocalDate mesReferencia
) {
}
