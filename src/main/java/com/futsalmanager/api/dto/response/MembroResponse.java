package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record MembroResponse(
        UUID membroId,
        UUID usuarioId,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo,
        LocalDateTime dataCriacao
) {
}
