package com.futsalmanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaAbatimentoCreateRequest(

        @Schema(description = "Valor pago neste lançamento", example = "150.00")
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @Schema(
                description = "Data em que o pagamento foi feito (padrão: hoje)",
                example = "2026-04-10",
                type = "string",
                format = "date"
        )
        LocalDate dataPagamento,

        @Schema(description = "Observação sobre o lançamento", example = "Pix repassado pelo tesoureiro")
        @Size(max = 255)
        String observacao
) {
}
