package com.futsalmanager.model.repositories;

import com.futsalmanager.model.entities.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TimeRepository extends JpaRepository<Time, UUID> {
}
