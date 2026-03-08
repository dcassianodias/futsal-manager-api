package com.futsalmanager.api.dto.request;

import java.util.UUID;

public record UsuarioUpdateRequest(

        String nome,
        String email
) {
}
