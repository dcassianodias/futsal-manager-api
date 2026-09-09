package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.VotoMelhorRodada;
import com.futsalmanager.domain.enums.QuadroTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotoMelhorRodadaRepository extends JpaRepository<VotoMelhorRodada, UUID> {

    boolean existsByJogoIdAndVotanteIdAndQuadro(UUID jogoId, UUID votanteId, QuadroTime quadro);

    Optional<VotoMelhorRodada> findByJogoIdAndVotanteIdAndQuadro(UUID jogoId, UUID votanteId, QuadroTime quadro);

    @Query("SELECT v.votado.id AS usuarioId, v.votado.nome AS nome, COUNT(v) AS votos " +
           "FROM VotoMelhorRodada v WHERE v.jogo.id = :jogoId AND v.quadro = :quadro " +
           "GROUP BY v.votado.id, v.votado.nome ORDER BY COUNT(v) DESC")
    List<VotoContagemProjection> contarPorJogoEQuadro(@Param("jogoId") UUID jogoId, @Param("quadro") QuadroTime quadro);
}
