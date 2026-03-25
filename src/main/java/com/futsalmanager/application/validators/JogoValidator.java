package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JogoValidator {

    private final JogoRepository jogoRepository;

    public JogoValidator(JogoRepository jogoRepository) {
        this.jogoRepository = jogoRepository;
    }

    public void validarCreate(JogoCreateRequest request) {
        validarDuplicidadeCreate(request);
    }

    public void validarUpdate(UUID id, JogoUpdateRequest request, Jogo entity) {
        validarStatusFinalizado(entity);
        validarDuplicidadeUpdate(id, request, entity);
    }

    private void validarDuplicidadeCreate(JogoCreateRequest request) {
        boolean existe = jogoRepository.existsByTimeIdAndAdversarioAndLocalAndDataHoraAndStatusJogoNot(
                request.timeId(),
                request.adversario(),
                request.local(),
                request.dataHora(),
                StatusJogo.CANCELADO
        );

        if (existe) {
            throw new BusinessException(
                    "Já existe um jogo com mesmo adversário, local e data/hora para este time"
            );
        }
    }

    private void validarDuplicidadeUpdate(UUID id, JogoUpdateRequest request, Jogo entity) {
        boolean duplicado = jogoRepository.existsByTimeIdAndAdversarioAndLocalAndDataHoraAndIdNotAndStatusJogoNot(
                entity.getTime().getId(),
                request.adversario(),
                request.local(),
                request.dataHora(),
                id,
                StatusJogo.CANCELADO
        );

        if (duplicado) {
            throw new BusinessException(
                    "Já existe outro jogo com mesmo adversário, local e data/hora para este time"
            );
        }
    }

    private void validarStatusFinalizado(Jogo jogo) {
        if (jogo.getStatusJogo() == StatusJogo.FINALIZADO) {
            throw new BusinessException(
                    "Não é permitido alterar um jogo finalizado"
            );
        }
    }
}
