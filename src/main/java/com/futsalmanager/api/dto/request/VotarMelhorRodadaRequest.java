package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.QuadroTime;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VotarMelhorRodadaRequest(

        @NotNull(message = "Usuário votado é obrigatório")
        UUID votadoId,

        @NotNull(message = "Quadro é obrigatório")
        QuadroTime quadro

) {
}
