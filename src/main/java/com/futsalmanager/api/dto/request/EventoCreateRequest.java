package com.futsalmanager.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventoCreateRequest(

        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @NotBlank(message = "Nome do evento é obrigatório")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "Descrição do evento é obrigatória")
        String descricao,

        BigDecimal valorSugerido,

        @NotNull(message = "Data inicial é obrigatória")
        LocalDate dataInicio,

        @NotNull(message = "Data final é obrigatória")
        LocalDate dataFim

) {
}
