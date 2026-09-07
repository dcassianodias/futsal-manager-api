package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(

        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email
) {
}
