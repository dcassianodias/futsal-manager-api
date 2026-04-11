package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.JogoMapper;
import com.futsalmanager.application.validators.JogoValidator;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class JogoService {

    private final JogoRepository jogoRepository;
    private final TimeRepository timeRepository;
    private final JogoMapper jogoMapper;
    private final JogoValidator validator;

    public JogoService(JogoRepository jogoRepository, TimeRepository timeRepository, JogoMapper jogoMapper,
                       JogoValidator validator) {
        this.jogoRepository = jogoRepository;
        this.timeRepository = timeRepository;
        this.jogoMapper = jogoMapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public JogoResponse findById(UUID id) {
        return jogoMapper.toResponse(buscarOuErro(id));
    }

    @Transactional(readOnly = true)
    public List<JogoResponse> findAll() {
        return jogoMapper.toResponseList(
                jogoRepository.findByStatusJogoNotOrderByDataHoraDesc(StatusJogo.CANCELADO));
    }

    @Transactional(readOnly = true)
    public List<JogoResponse> findByTime(UUID timeId){
        return jogoMapper.toResponseList(
                jogoRepository.findByTimeIdAndStatusJogoNotOrderByDataHoraDesc(
                timeId, StatusJogo.CANCELADO));
    }

    @Transactional
    public JogoResponse create(JogoCreateRequest request){
        validator.validarCreate(request);

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + request.timeId()));

        Jogo jogo = jogoMapper.toEntity(request);
        jogo.setTime(time);

        Jogo saved = jogoRepository.save(jogo);

        log.info("Jogo criado com sucesso: id={}, adversário={}", saved.getId(), saved.getAdversario());

        return jogoMapper.toResponse(saved);
    }

    @Transactional
    public JogoResponse update(UUID id, JogoUpdateRequest request) {
        Jogo entity = buscarOuErro(id);

        String adversario = request.adversario() != null ? request.adversario() : entity.getAdversario();
        String local = request.local() != null ? request.local() : entity.getLocal();
        LocalDateTime dataHora = request.dataHora() != null ? request.dataHora() : entity.getDataHora();

        validator.validarUpdate(id, request, entity);

        boolean duplicado = jogoRepository.existsByTimeIdAndAdversarioAndLocalAndDataHoraAndIdNotAndStatusJogoNot(
                entity.getTime().getId(),
                adversario,
                local,
                dataHora,
                id,
                StatusJogo.CANCELADO
        );

        if(duplicado){
            throw new BusinessException(
                    "Já existe outro jogo com mesmo adversário, local e data/hora para este time"
            );
        }

        jogoMapper.updateEntityFromRequest(request, entity);

        Jogo updated = jogoRepository.save(entity);

        log.info("Jogo atualizado: id={}, adversário={}, status={}", updated.getId(),
                updated.getAdversario(), updated.getStatusJogo());

        return jogoMapper.toResponse(updated);
    }

    @Transactional
    public JogoResponse finalizar(UUID id) {
        Jogo entity = buscarOuErro(id);

        if (entity.getStatusJogo() != StatusJogo.AGENDADO) {
            throw new BusinessException("Somente jogos agendados podem ser finalizados");
        }

        entity.setStatusJogo(StatusJogo.FINALIZADO);

        Jogo saved = jogoRepository.save(entity);

        log.info("Jogo finalizado: id={}, adversário={}", saved.getId(), saved.getAdversario());

        return jogoMapper.toResponse(saved);
    }

    @Transactional
    public JogoResponse cancelar(UUID id) {
        Jogo entity = buscarOuErro(id);

        if (entity.getStatusJogo() != StatusJogo.AGENDADO) {
            throw new BusinessException("Somente jogos agendados podem ser cancelados");
        }

        entity.setStatusJogo(StatusJogo.CANCELADO);

        log.info("Jogo cancelado: id={}, adversário={}", entity.getId(), entity.getAdversario());

        return jogoMapper.toResponse(jogoRepository.save(entity));

    }

    private Jogo buscarOuErro(UUID id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + id));
    }

}
