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

    List<Jogo> findByTimeIdAndStatusJogoNotOrderByDataHoraAsc(UUID timeId, StatusJogo status);

    List<Jogo> findByStatusJogoNotOrderByDataHoraAsc(StatusJogo status);

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
}
