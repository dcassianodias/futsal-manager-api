package com.futsalmanager.api.dto.request;

import com.futsalmanager.domain.enums.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoCreateRequest(
        @NotNull(message = "Time é obrigatório")
        UUID timeId,

        @NotNull(message = "Usuário é obrigatório")
        UUID usuarioId,

        UUID eventoId,

        LocalDate mesReferencia,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Tipo de pagamento é obrigatório")
        TipoPagamento tipo

) {
}
