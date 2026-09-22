package com.futsalmanager.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DespesaAbatimentoResponse(
        UUID id,
        UUID despesaId,
        BigDecimal valor,
        LocalDate dataPagamento,
        String observacao,
        String registradoPorNome,
        LocalDateTime dataCriacao
) {
}
