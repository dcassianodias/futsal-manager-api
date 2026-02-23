package com.futsalmanager.api.dto.request;

import java.math.BigDecimal;

public record TimeCreateRequest(
        String nome,
        BigDecimal valorMensalidade,
        Boolean ativo

) {
}
