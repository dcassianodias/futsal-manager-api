package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.StatusDespesa;
import com.futsalmanager.domain.enums.TipoDespesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DespesaResponse(
        UUID id,
        UUID timeId,
        String descricao,
        BigDecimal valor,
        LocalDate mesReferencia,
        TipoDespesa tipoDespesa,
        StatusDespesa status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
