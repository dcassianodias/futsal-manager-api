package com.futsalmanager.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeResponse(
        UUID id,
        String nome,
        String codigo,
        BigDecimal valorMensalidade,
        Boolean ativo,
        Boolean publico,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
