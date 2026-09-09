package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record FinalizarJogoRequest(

        @NotNull(message = "Gols do time no 1º quadro é obrigatório")
        @Min(value = 0, message = "Gols não pode ser negativo")
        Integer golsTimeQuadro1,

        @NotNull(message = "Gols do adversário no 1º quadro é obrigatório")
        @Min(value = 0, message = "Gols não pode ser negativo")
        Integer golsAdversarioQuadro1,

        List<UUID> artilheirosQuadro1,

        @NotNull(message = "Gols do time no 2º quadro é obrigatório")
        @Min(value = 0, message = "Gols não pode ser negativo")
        Integer golsTimeQuadro2,

        @NotNull(message = "Gols do adversário no 2º quadro é obrigatório")
        @Min(value = 0, message = "Gols não pode ser negativo")
        Integer golsAdversarioQuadro2,

        List<UUID> artilheirosQuadro2

) {
}
