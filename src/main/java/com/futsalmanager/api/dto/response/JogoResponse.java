package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.ResultadoJogo;
import com.futsalmanager.domain.enums.StatusJogo;

import java.time.LocalDateTime;
import java.util.UUID;

public record JogoResponse(
        UUID id,
        UUID timeId,
        String adversario,
        String local,
        LocalDateTime dataHora,
        StatusJogo statusJogo,
        Integer golsTime,
        Integer golsAdversario,
        ResultadoJogo resultado,
        String observacoes,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
