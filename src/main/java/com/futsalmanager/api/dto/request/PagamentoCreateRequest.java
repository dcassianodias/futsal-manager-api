package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoCreateRequest(
        UUID timeId,
        UUID usuarioId,
        UUID eventoId,
        LocalDate mesReferencia,
        BigDecimal valor,
        TipoPagamento tipo

) {
}
