package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.NotNull;

public record AlterarPerfilMembroRequest(
        @NotNull(message = "Perfil é obrigatório")
        PerfilUsuario perfil
) {
}
