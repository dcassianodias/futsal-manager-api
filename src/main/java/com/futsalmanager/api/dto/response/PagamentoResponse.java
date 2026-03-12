package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.StatusPagamento;
import com.futsalmanager.domain.enums.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID timeId,
        UUID usuarioId,
        UUID eventoId,
        LocalDate mesReferencia,
        BigDecimal valor,
        TipoPagamento tipo,
        StatusPagamento status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
