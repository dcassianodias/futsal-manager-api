package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.TipoDespesa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaUpdateRequest(

        @Size(max = 255)
        String descricao,

        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        LocalDate mesReferencia,

        TipoDespesa tipoDespesa

) {
}
