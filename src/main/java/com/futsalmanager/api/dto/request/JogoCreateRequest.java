package com.futsalmanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados para criação de um jogo")
public record JogoCreateRequest(

        @Schema(
                description = "ID do time mandante",
                example = "f3787599-81fe-4eb5-9f58-8877b0ac9998"
        )
        @NotNull(message = "Time mandante é obrigatório")
        UUID timeId,

        @Schema(
                description = "Nome do time adversário",
                example = "Os Amigos FC"
        )
        @NotBlank(message = "Nome do adversário é obrigatório")
        String adversario,

        @Schema(
                description = "Local onde o jogo será realizado",
                example = "Quadra do Ramos"
        )
        @NotBlank(message = "Local do jogo é obrigatório")
        String local,

        @Schema(
                description = "Data e hora do jogo",
                example = "22/10/2026 10:00"
        )
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        @NotNull(message = "Data e hora do jogo são obrigatórios")
        LocalDateTime dataHora,

        @Schema(
                description = "Observações adicionais sobre o jogo",
                example = "Levar uniforme reserva",
                nullable = true
        )
        String observacoes
) {}