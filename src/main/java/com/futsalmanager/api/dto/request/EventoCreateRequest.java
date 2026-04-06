package com.futsalmanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventoCreateRequest(

        @Schema(description = "ID do time organizador do evento", example = "f3787599-81fe-4eb5-9f58-8877b0ac9998")
        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @Schema(description = "Nome do evento", example = "Torneio de Futsal de Verão")
        @NotBlank(message = "Nome do evento é obrigatório")
        @Size(max = 150)
        String nome,

        @Schema(description = "Descrição do evento", example = "Torneio de futsal para equipes amadoras durante o verão")
        @NotBlank(message = "Descrição do evento é obrigatória")
        @Size(max = 150)
        String descricao,

        @Schema(description = "Valor sugerido por participante", example = "50.00")
        BigDecimal valorSugerido,

        @Schema(description = "Data de início do evento", example = "2024-07-01")
        @NotNull(message = "Data inicial é obrigatória")
        LocalDate dataInicio,

        @Schema(description = "Data de término do evento", example = "2024-07-07")
        @NotNull(message = "Data final é obrigatória")
        LocalDate dataFim

) {
}
