package com.futsalmanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.futsalmanager.domain.enums.TipoPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Dados para criação de um pagamento")
public record PagamentoCreateRequest(

        @Schema(
                description = "ID do time",
                example = "f3787599-81fe-4eb5-9f58-8877b0ac9998"
        )
        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @Schema(
                description = "ID do usuário que realizará o pagamento",
                example = "c1234567-1234-1234-1234-123456789abc"
        )
        @NotNull(message = "Usuário é obrigatório")
        UUID usuarioId,

        @Schema(
                description = "ID do evento (obrigatório quando tipo = EVENTO)",
                example = "a9876543-4321-4321-4321-cba987654321",
                nullable = true
        )
        UUID eventoId,

        @Schema(
                description = "Mês de referência (obrigatório quando tipo = MENSALIDADE)",
                example = "01/04/2026",
                nullable = true
        )
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate mesReferencia,

        @Schema(
                description = "Valor do pagamento",
                example = "50.00"
        )
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @Schema(
                description = "Tipo do pagamento",
                example = "MENSALIDADE",
                allowableValues = {"MENSALIDADE", "EVENTO"}
        )
        @NotNull(message = "Tipo de pagamento é obrigatório")
        TipoPagamento tipo

) {}