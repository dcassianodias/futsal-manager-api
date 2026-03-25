package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GerarMensalidadeRequest(

        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @NotNull(message = "Mês de referência é obrigatório")
        LocalDate mesReferencia
) {
}
