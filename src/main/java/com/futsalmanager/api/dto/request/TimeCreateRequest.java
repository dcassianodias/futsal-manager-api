package com.futsalmanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados para criação de um time")
public record TimeCreateRequest(

        @Schema(
                description = "Nome do time",
                example = "Os Craques FC"
        )
        @NotBlank(message = "Nome do time é obrigatório")
        String nome,

        @Schema(
                description = "Valor da mensalidade do time",
                example = "50.00"
        )
        @DecimalMin(value = "0.01", message = "Valor da mensalidade deve ser maior que zero")
        BigDecimal valorMensalidade
) {}