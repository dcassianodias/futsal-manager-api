package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(

        @Size(max = 150)
        String nome,

        @Email(message = "Email inválido")
        String email,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {
}
