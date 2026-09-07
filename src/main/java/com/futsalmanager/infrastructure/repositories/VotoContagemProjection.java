package com.futsalmanager.infrastructure.repositories;

import java.util.UUID;

public interface VotoContagemProjection {
    UUID getUsuarioId();
    String getNome();
    Long getVotos();
}
