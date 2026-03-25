package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioUpdateRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "O email do usuário é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {
}