package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegisterRequest(

        @NotBlank(message = "Nome do time é obrigatório")
        @Size(max = 150, message = "Nome do time deve ter no máximo 150 caracteres")
        String nomeTime,

        @DecimalMin(value = "0.01", message = "Valor da mensalidade deve ser maior que zero")
        BigDecimal valorMensalidade,

        @NotBlank(message = "Nome do administrador é obrigatório")
        @Size(max = 150, message = "Nome do administrador deve ter no máximo 150 caracteres")
        String nomeAdmin,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha
) {
}
