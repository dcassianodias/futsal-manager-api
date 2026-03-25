package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TimeCreateRequest(

        @NotBlank(message = "Nome do time é obrigatório")
        String nome,

        @NotNull(message = "Valor da mensalidade é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valorMensalidade
) {
}
