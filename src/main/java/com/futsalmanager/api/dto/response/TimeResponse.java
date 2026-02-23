package com.futsalmanager.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeResponse(
        UUID id,
        String nome,
        BigDecimal valorMensalidade,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
