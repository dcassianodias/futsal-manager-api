package com.futsalmanager.api.dto.response;

import com.futsalmanager.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        UUID timeId,
        PerfilUsuario perfil,
        Boolean ativo,
        Integer gols,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
