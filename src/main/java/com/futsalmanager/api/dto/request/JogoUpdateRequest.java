package com.futsalmanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.futsalmanager.domain.enums.StatusJogo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;

public record JogoUpdateRequest(

        @NotNull(message = "Time mandante é obrigatório.")
        UUID timeId,

        @NotBlank(message = "Nome do adversário é obrigatório.")
        String adversario,

        @NotBlank(message = "Local do jogo é obrigatório.")
        String local,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        @NotNull(message = "Data e hora do jogo são obrigatórios.")
        LocalDateTime dataHora,

        StatusJogo status,
        String observacoes
) {
}
