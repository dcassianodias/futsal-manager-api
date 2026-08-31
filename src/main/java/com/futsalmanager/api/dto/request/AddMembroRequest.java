package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para adicionar um membro (existente ou novo) a um time")
public record AddMembroRequest(

        @Schema(description = "Email do membro. Se já existir uma conta com esse email, ela é reaproveitada.")
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email,

        @Schema(description = "Nome do membro. Obrigatório apenas se ainda não existir conta com esse email.")
        String nome,

        @Schema(description = "Senha inicial. Obrigatória apenas se ainda não existir conta com esse email.")
        String senha,

        @Schema(description = "Perfil do membro no time (padrão ATLETA se omitido)")
        PerfilUsuario perfil
) {
}
