package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoUpdateRequest(
        UUID eventoId,
        LocalDate mesReferencia,
        BigDecimal valor
) {
}
