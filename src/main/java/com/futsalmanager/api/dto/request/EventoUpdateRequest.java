package com.futsalmanager.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventoUpdateRequest(
        UUID timeId,
        String nome,
        String descricao,
        BigDecimal valorSugerido,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
