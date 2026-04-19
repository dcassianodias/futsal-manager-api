package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class JogoValidator {

    private final JogoRepository jogoRepository;

    public JogoValidator(JogoRepository jogoRepository) {
        this.jogoRepository = jogoRepository;
    }

    public void validarCreate(JogoCreateRequest request) {
        String adversarioNormalizado = request.adversario().trim().toUpperCase();

        validarDataHora(request.dataHora());

        validarConflitoAgenda(
                request.timeId(),
                request.adversario(),
                request.dataHora(),
                null
        );
    }

    public void validarUpdate(UUID id, JogoUpdateRequest request, Jogo entity) {
        validarStatusAlteracao(entity);

        String adversarioFinal = request.adversario() != null
                ? request.adversario().trim().toUpperCase()
                : entity.getAdversario();

        LocalDateTime dataHoraFinal = request.dataHora() != null
                ? request.dataHora()
                : entity.getDataHora();

        validarDataHora(dataHoraFinal);

        if (request.dataHora() != null || request.adversario() != null) {
            validarConflitoAgenda(
                    entity.getTime().getId(),
                    adversarioFinal,
                    dataHoraFinal,
                    id
            );
        }
    }

    private void validarDataHora(LocalDateTime dataHora) {
        if (dataHora.isBefore(LocalDateTime.now().plusMinutes(1))) {
            throw new BusinessException("Data e hora do jogo não podem ser no passado");
        }
    }

    /**
     * REGRA CENTRAL:
     * - Time principal não pode ter jogo no mesmo horário
     * - Adversário não pode ter jogo no mesmo horário
     */
    private void validarConflitoAgenda(UUID timeId, String adversario, LocalDateTime dataHora, UUID ignoreId) {

        boolean timeOcupado;
        boolean adversarioOcupado;

        if (ignoreId == null) {
            // CREATE
            timeOcupado = jogoRepository.existsByTimeIdAndDataHoraAndStatusJogoNot(
                    timeId,
                    dataHora,
                    StatusJogo.CANCELADO
            );

            adversarioOcupado = jogoRepository.existsByAdversarioAndDataHoraAndStatusJogoNot(
                    adversario,
                    dataHora,
                    StatusJogo.CANCELADO
            );

        } else {
            // UPDATE
            timeOcupado = jogoRepository.existsByTimeIdAndDataHoraAndIdNotAndStatusJogoNot(
                    timeId,
                    dataHora,
                    ignoreId,
                    StatusJogo.CANCELADO
            );

            adversarioOcupado = jogoRepository.existsByAdversarioAndDataHoraAndIdNotAndStatusJogoNot(
                    adversario,
                    dataHora,
                    ignoreId,
                    StatusJogo.CANCELADO
            );
        }

        if (timeOcupado) {
            throw new BusinessException("O time já possui um jogo neste horário");
        }

        if (adversarioOcupado) {
            throw new BusinessException("O adversário já possui um jogo neste horário");
        }
    }

    private void validarStatusAlteracao(Jogo jogo) {
        if (jogo.getStatusJogo() != StatusJogo.AGENDADO) {
            throw new BusinessException("Só é possível alterar jogos agendados");
        }
    }

    public void validarPodeCancelar(Jogo entity) {
        validarStatusAgendado(entity, "cancelar");
    }

    public void validarPodeFinalizar(Jogo entity) {
        validarStatusAgendado(entity, "finalizar");
    }

    private void validarStatusAgendado(Jogo entity, String acao) {
        if (entity.getStatusJogo() != StatusJogo.AGENDADO) {
            throw new BusinessException("Só é possível " + acao + " jogos agendados");
        }
    }
}