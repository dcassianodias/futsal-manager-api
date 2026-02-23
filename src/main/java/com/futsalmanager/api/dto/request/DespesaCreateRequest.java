package com.futsalmanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.futsalmanager.domain.enums.TipoDespesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaCreateRequest(
        UUID timeId,
        String descricao,
        BigDecimal valor,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate mesReferencia,

        TipoDespesa tipo
) {
}
