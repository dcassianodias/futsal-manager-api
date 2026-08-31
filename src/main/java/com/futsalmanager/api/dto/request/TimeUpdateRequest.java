package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TimeUpdateRequest(

        String nome,

        @DecimalMin(value = "0.00", message = "Valor não pode ser negativo")
        BigDecimal valorMensalidade,

        Boolean ativo

) {
}
