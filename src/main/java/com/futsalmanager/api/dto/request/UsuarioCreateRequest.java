package com.futsalmanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Dados para criação de usuário")
public record UsuarioCreateRequest(

        @Schema(
                description = "ID do time ao qual o usuário pertence",
                example = "f3787599-81fe-4eb5-9f58-8877b0ac9998"
        )
        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @Schema(
                description = "Nome do usuário",
                example = "Danilo Cassiano Dias"
        )
        @NotBlank(message = "O nome do usuário é obrigatório")
        @Size(max = 150, message = "O nome do usuário deve ter no máximo 150 caracteres")
        String nome,

        @Schema(
                description = "Email do usuário",
                example = "danilo@email.com"
        )
        @Email(message = "Email inválido")
        @NotBlank(message = "O email do usuário é obrigatório")
        String email,

        @Schema(
                description = "Senha do usuário",
                example = "123456"
        )
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {}