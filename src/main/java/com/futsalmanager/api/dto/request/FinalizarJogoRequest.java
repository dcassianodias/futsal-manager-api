package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record FinalizarJogoRequest(

        @NotNull(message = "Gols do time é obrigatório")
        @Min(value = 0, message = "Gols não pode ser negativo")
        Integer golsTime,

        @NotNull(message = "Gols do adversário é obrigatório")
        @Min(value = 0, message = "Gols não pode ser negativo")
        Integer golsAdversario,

        List<UUID> artilheiros

) {
}
