package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.mappers.JogoMapper;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class JogoService {

    private final JogoRepository jogoRepository;
    private final TimeRepository timeRepository;
    private final JogoMapper jogoMapper;

    public JogoService(JogoRepository jogoRepository, TimeRepository timeRepository, JogoMapper jogoMapper) {
        this.jogoRepository = jogoRepository;
        this.timeRepository = timeRepository;
        this.jogoMapper = jogoMapper;
    }

    @Transactional(readOnly = true)
    public JogoResponse findById(UUID id) {
        Jogo entity = jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado: " + id));
        return jogoMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<JogoResponse> findAll() {
        List<Jogo> list = jogoRepository.findAll();
        return jogoMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<JogoResponse> findByTime(UUID timeId){
        List<Jogo> list = jogoRepository.findByTimeIdOrderByDataHoraDesc(timeId);
        return jogoMapper.toResponseList(list);
    }

    @Transactional
    public JogoResponse create(JogoCreateRequest request){
        if (request.timeId() == null){
            throw new IllegalArgumentException("Time mandante é obrigatório.");
        }

        if (request.adversario() == null || request.adversario().isBlank()){
            throw new IllegalArgumentException("Nome do adversário é obrigatório.");
        }

        if(request.local() == null || request.local().isBlank()){
            throw new IllegalArgumentException("Local do jogo é obrigatório.");
        }

        if(request.dataHora() == null){
            throw new IllegalArgumentException("O horário do jogo é obrigatório.");
        }

        boolean jogoJaExiste = jogoRepository.existsByTimeIdAndAdversarioAndLocalAndDataHora(
                request.timeId(),
                request.adversario(),
                request.local(),
                request.dataHora()
        );

        if (jogoJaExiste){
            throw new IllegalArgumentException("Já existe um jogo agendado com esse adversário, local e data/hora " +
                    " para o time informado");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        Jogo jogo = jogoMapper.toEntity(request);
        jogo.setStatusJogo(StatusJogo.AGENDADO);
        jogo.setTime(time);

        Jogo saved = jogoRepository.save(jogo);

        log.info("Jogo criado com sucesso: id={}, adversário={}", saved.getId(), saved.getAdversario());

        return jogoMapper.toResponse(saved);
    }

    @Transactional
    public JogoResponse update(UUID id, JogoUpdateRequest request) {
        Jogo entity = jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado: " + id));

        if (request.timeId() == null){
            throw new IllegalArgumentException("Time mandante é obrigatório.");
        }

        if (request.adversario() == null || request.adversario().isBlank()){
            throw new IllegalArgumentException("Nome do adversário é obrigatório.");
        }

        if (request.local() == null || request.local().isBlank()){
            throw new IllegalArgumentException("Local do jogo é obrigatório.");
        }

        if (request.dataHora() == null){
            throw new IllegalArgumentException("O horário do jogo é obrigatório.");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        entity.setTime(time);
        entity.setAdversario(request.adversario());
        entity.setLocal(request.local());
        entity.setDataHora(request.dataHora());
        entity.setObservacoes(request.observacoes());

        if (entity.getStatusJogo() == StatusJogo.FINALIZADO){
            throw new IllegalStateException("Não é permitido atualizar um jogo que já foi finalizado.");
        }

        if (request.status() != null) {
            entity.setStatusJogo(request.status());
        }

        boolean jogoDuplicado = jogoRepository.existsByTimeIdAndAdversarioAndLocalAndDataHoraAndIdNot(
                request.timeId(),
                request.adversario(),
                request.local(),
                request.dataHora(),
                id
        );

        if (jogoDuplicado){
            throw new IllegalArgumentException("Já existe outro jogo cadastrado com esse adversário, " +
                    "local e data/hora para o time informado");
        }

        Jogo updated = jogoRepository.save(entity);

        log.info("Jogo atualizado com sucesso: id={}, adversário={}, status={}", updated.getId(),
                updated.getAdversario(), updated.getStatusJogo());

        return jogoMapper.toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Jogo entity = jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado: " + id));
        jogoRepository.delete(entity);

        log.info("Jogo deletado com sucesso: id={}, adversário={}", entity.getId(), entity.getAdversario());
    }

}
