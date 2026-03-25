package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioCreateRequest(

        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @NotBlank(message = "O nome do usuário é obrigatório")
        @Size(max = 150, message = "O nome do usuário deve ter no máximo 150 caracteres")
        String nome,

        @Email(message = "Email inválido")
        @NotBlank(message = "O email do usuário é obrigatório")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {
}
