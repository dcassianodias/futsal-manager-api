package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.TipoDespesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaUpdateRequest(

        UUID timeId,
        String descricao,
        BigDecimal valor,
        LocalDate mesReferencia,
        TipoDespesa tipoDespesa

) {
}
