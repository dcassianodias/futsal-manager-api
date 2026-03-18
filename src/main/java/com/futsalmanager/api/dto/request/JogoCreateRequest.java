package com.futsalmanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record JogoCreateRequest(
        UUID timeId,
        String adversario,
        String local,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHora,

        String observacoes
) {
}
