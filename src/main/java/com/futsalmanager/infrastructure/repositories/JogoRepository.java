package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.enums.StatusJogo;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Jogo> findByTimeIdAndStatusJogoNotOrderByDataHoraDesc(UUID timeId, StatusJogo status);

    List<Jogo> findByStatusJogoNotOrderByDataHoraDesc(StatusJogo status);

    boolean existsByTimeIdAndAdversarioAndLocalAndDataHoraAndStatusJogoNot(
            UUID timeId,
            String adversario,
            String local,
            LocalDateTime dataHora,
            StatusJogo status
    );
}
