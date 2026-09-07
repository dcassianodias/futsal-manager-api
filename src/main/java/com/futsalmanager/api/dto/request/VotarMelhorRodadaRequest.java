package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VotarMelhorRodadaRequest(

        @NotNull(message = "Usuário votado é obrigatório")
        UUID votadoId

) {
}
