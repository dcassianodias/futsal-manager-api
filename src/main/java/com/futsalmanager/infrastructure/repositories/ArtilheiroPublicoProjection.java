package com.futsalmanager.infrastructure.repositories;

import java.util.UUID;

public interface ArtilheiroPublicoProjection {
    UUID getUsuarioId();
    String getNome();
    String getTimeNome();
    Long getGols();
}
