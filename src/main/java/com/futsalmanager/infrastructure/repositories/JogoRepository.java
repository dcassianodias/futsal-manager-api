package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.enums.ResultadoJogo;
import com.futsalmanager.domain.enums.StatusJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JogoRepository extends JpaRepository<Jogo, UUID> {

    boolean existsByTimeIdAndAdversarioAndLocalAndDataHoraAndIdNotAndStatusJogoNot(
            UUID timeId,
            String adversario,
            String local,
            LocalDateTime dataHora,
            UUID id,
            StatusJogo status
    );

    List<Jogo> findByTimeIdAndStatusJogoNotOrderByDataHoraAsc(UUID timeId, StatusJogo status);

    List<Jogo> findByTimeIdAndStatusJogoOrderByDataHoraAsc(UUID timeId, StatusJogo statusJogo);

    List<Jogo> findByTimeIdAndStatusJogoOrderByDataHoraDesc(UUID timeId, StatusJogo statusJogo);

    List<Jogo> findByStatusJogoNotOrderByDataHoraAsc(StatusJogo status);

    long countByStatusJogo(StatusJogo statusJogo);

    boolean existsByTimeIdAndAdversarioAndLocalAndDataHoraAndStatusJogoNot(
            UUID timeId,
            String adversario,
            String local,
            LocalDateTime dataHora,
            StatusJogo status
    );

    boolean existsByTimeIdAndDataHoraAndStatusJogoNot(UUID timeId, LocalDateTime dataHora, StatusJogo statusJogo);

    boolean existsByAdversarioAndDataHoraAndStatusJogoNot(String adversario, LocalDateTime dataHora,
                                                          StatusJogo statusJogo);

    boolean existsByTimeIdAndDataHoraAndIdNotAndStatusJogoNot(UUID timeId, LocalDateTime dataHora, UUID ignoreId,
                                                              StatusJogo statusJogo);

    boolean existsByAdversarioAndDataHoraAndIdNotAndStatusJogoNot(String adversario, LocalDateTime dataHora,
                                                                  UUID ignoreId, StatusJogo statusJogo);

    List<Jogo> findTop12ByTimePublicoTrueAndStatusJogoOrderByDataHoraDesc(StatusJogo statusJogo);

    List<Jogo> findTop2ByTimePublicoTrueAndStatusJogoOrderByDataHoraAsc(StatusJogo statusJogo);

    @Query("SELECT j.time.id AS timeId, " +
           "SUM(CASE WHEN j.resultado = :vitoria THEN 1L ELSE 0L END) AS vitorias, " +
           "COUNT(j) AS total " +
           "FROM Jogo j WHERE j.time.id IN :timeIds AND j.statusJogo = :finalizado AND j.resultado IS NOT NULL " +
           "GROUP BY j.time.id")
    List<AproveitamentoProjection> aproveitamentoPorTimes(@Param("timeIds") List<UUID> timeIds,
                                                           @Param("finalizado") StatusJogo finalizado,
                                                           @Param("vitoria") ResultadoJogo vitoria);
}
