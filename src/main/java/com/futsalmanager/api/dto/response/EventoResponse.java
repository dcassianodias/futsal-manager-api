package com.futsalmanager.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventoResponse(
        UUID id,
        UUID timeId,
        String nome,
        String descricao,
        BigDecimal valorSugerido,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
