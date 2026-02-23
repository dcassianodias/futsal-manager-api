package com.futsalmanager.api.dto.request;

import java.math.BigDecimal;

public record TimeUpdateRequest(
        String nome,
        BigDecimal valorMensalidade,
        Boolean ativo

) {
}
