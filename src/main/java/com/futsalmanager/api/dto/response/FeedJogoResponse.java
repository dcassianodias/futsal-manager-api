package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.StatusJogo;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedJogoResponse(
        UUID timeId,
        String timeNome,
        String adversario,
        StatusJogo status,
        LocalDateTime dataHora,
        Integer golsTime,
        Integer golsAdversario,
        String destaque,
        Integer aproveitamentoTime
) {
}
