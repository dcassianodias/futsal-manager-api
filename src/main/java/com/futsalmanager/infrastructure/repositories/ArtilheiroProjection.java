package com.futsalmanager.infrastructure.repositories;

import java.util.UUID;

public interface ArtilheiroProjection {
    UUID getUsuarioId();
    String getNome();
    Long getGols();
}
