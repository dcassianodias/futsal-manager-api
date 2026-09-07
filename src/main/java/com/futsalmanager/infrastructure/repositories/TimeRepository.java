package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeRepository extends JpaRepository<Time, UUID> {

    long countByAtivoTrue();

    Optional<Time> findByCodigo(String codigo);
}
