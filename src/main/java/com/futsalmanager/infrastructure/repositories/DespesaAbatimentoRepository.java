package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.DespesaAbatimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DespesaAbatimentoRepository extends JpaRepository<DespesaAbatimento, UUID> {

    List<DespesaAbatimento> findByDespesaIdOrderByDataPagamentoDesc(UUID despesaId);

}
