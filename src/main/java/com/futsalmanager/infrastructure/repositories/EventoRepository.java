package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {
}
