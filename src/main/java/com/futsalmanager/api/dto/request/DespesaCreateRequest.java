package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.TipoDespesa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaCreateRequest(

        @Schema(description = "ID do time", example = "f3787599-81fe-4eb5-9f58-8877b0ac9998")
        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @Schema(description = "Descrição da despesa", example = "Aluguel da quadra")
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 255)
        String descricao,

        @Schema(description = "Valor da despesa", example = "150.00")
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @Schema(
                description = "Mês de referência da despesa",
                example = "2026-04-01",
                type = "string",
                format = "date"
        )
        @NotNull(message = "Mês de referência é obrigatório")
        LocalDate mesReferencia,

        @Schema(
                description = "Tipo da despesa",
                example = "ALUGUEL_QUADRA"
        )
        @NotNull(message = "Tipo da despesa é obrigatório")
        TipoDespesa tipoDespesa
) {
}
