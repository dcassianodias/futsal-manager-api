package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.VotoMelhorRodada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotoMelhorRodadaRepository extends JpaRepository<VotoMelhorRodada, UUID> {

    boolean existsByJogoIdAndVotanteId(UUID jogoId, UUID votanteId);

    Optional<VotoMelhorRodada> findByJogoIdAndVotanteId(UUID jogoId, UUID votanteId);

    @Query("SELECT v.votado.id AS usuarioId, v.votado.nome AS nome, COUNT(v) AS votos " +
           "FROM VotoMelhorRodada v WHERE v.jogo.id = :jogoId " +
           "GROUP BY v.votado.id, v.votado.nome ORDER BY COUNT(v) DESC")
    List<VotoContagemProjection> contarPorJogo(@Param("jogoId") UUID jogoId);
}
