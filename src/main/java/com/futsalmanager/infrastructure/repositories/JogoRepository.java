package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JogoRepository extends JpaRepository<Jogo, UUID> {

    List<Jogo> findByTimeIdOrderByDataHoraDesc(UUID timeId);

    boolean existsByTimeIdAndAdversarioAndLocalAndDataHora(UUID timeId, String adversario, String local,
                                                           LocalDateTime dataHora);

    boolean existsByTimeIdAndAdversarioAndLocalAndDataHoraAndIdNot(
            UUID timeId,
            String adversario,
            String local,
            LocalDateTime dataHora,
            UUID id
    );
}
