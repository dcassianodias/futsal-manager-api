package com.futsalmanager.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
