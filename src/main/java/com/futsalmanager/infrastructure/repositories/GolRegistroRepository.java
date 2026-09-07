package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.GolRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GolRegistroRepository extends JpaRepository<GolRegistro, UUID> {

    List<GolRegistro> findByJogoId(UUID jogoId);

    List<GolRegistro> findByJogoIdIn(List<UUID> jogoIds);

    @Query("SELECT g.usuario.id AS usuarioId, g.usuario.nome AS nome, SUM(g.quantidade) AS gols " +
           "FROM GolRegistro g WHERE g.jogo.time.id = :timeId " +
           "GROUP BY g.usuario.id, g.usuario.nome ORDER BY SUM(g.quantidade) DESC")
    List<ArtilheiroProjection> rankingPorTime(@Param("timeId") UUID timeId);

    @Query("SELECT g.usuario.id AS usuarioId, g.usuario.nome AS nome, g.jogo.time.nome AS timeNome, SUM(g.quantidade) AS gols " +
           "FROM GolRegistro g WHERE g.jogo.time.publico = true " +
           "GROUP BY g.usuario.id, g.usuario.nome, g.jogo.time.nome ORDER BY SUM(g.quantidade) DESC")
    List<ArtilheiroPublicoProjection> rankingPublico();
}
