package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, UUID> {

    List<Despesa> findByTimeIdOrderByMesReferenciaDesc(UUID timeId);

    List<Despesa>findByTimeIdAndMesReferencia(UUID timeId, LocalDate mesReferencia);
}
