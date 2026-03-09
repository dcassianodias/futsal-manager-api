package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.application.mappers.EventoMapper;
import com.futsalmanager.domain.entities.Evento;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.EventoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EventoService {

    private final EventoRepository eventoRepository;
    private final TimeRepository timeRepository;
    private final EventoMapper eventoMapper;

    public EventoService(EventoRepository eventoRepository, TimeRepository timeRepository,
                          EventoMapper eventoMapper) {
        this.eventoRepository = eventoRepository;
        this.timeRepository = timeRepository;
        this.eventoMapper = eventoMapper;
    }

    @Transactional(readOnly = true)
    public EventoResponse findById(UUID id) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + id));
        return eventoMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> findAll() {
        List<Evento> list = eventoRepository.findAll();
        return eventoMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> findByTime(UUID timeId){
        List<Evento> list = eventoRepository.findByTimeIdOrderByDataInicioDesc(timeId);
        return eventoMapper.toResponseList(list);
    }

    @Transactional
    public EventoResponse create(EventoCreateRequest request){
        if (request.timeId() == null){
            throw new IllegalArgumentException("Time do evento é obrigatório.");
        }

        if (request.nome() == null || request.nome().isBlank()){
            throw new IllegalArgumentException("Nome do evento é obrigatório.");
        }

        if(request.descricao() == null || request.descricao().isBlank()){
            throw new IllegalArgumentException("Descrição do evento é obrigatória.");
        }

        if(request.dataInicio() == null){
            throw new IllegalArgumentException("Data inicial do evento é obrigatória.");
        }

        if (request.dataFim() == null){
            throw new IllegalArgumentException("Data fim do evento é obrigatória.");
        }

        if (request.dataFim().isBefore(request.dataInicio())){
            throw new IllegalArgumentException("Data fim do evento não pode ser anterior à data início.");
        }

        if (request.valorSugerido() != null && request.valorSugerido().signum() < 0){
            throw new IllegalArgumentException("Valor sugerido do evento não pode ser negativo.");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        Evento evento = eventoMapper.toEntity(request);
        evento.setTime(time);
        evento.setAtivo(true);

        Evento saved = eventoRepository.save(evento);

        log.info("Evento salvo com sucesso: id={}, nome={}", saved.getId(), saved.getNome());

        return eventoMapper.toResponse(saved);
    }

    @Transactional
    public EventoResponse update(UUID id, EventoUpdateRequest request) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + id));

        if (request.timeId() == null){
            throw new IllegalArgumentException("Time do evento é obrigatório.");
        }

        if (request.nome() == null || request.nome().isBlank()){
            throw new IllegalArgumentException("Nome do evento é obrigatório.");
        }

        if(request.descricao() == null || request.descricao().isBlank()){
            throw new IllegalArgumentException("Descrição do evento é obrigatória.");
        }

        if (request.valorSugerido() == null){
            throw new IllegalArgumentException("Valor sugerido do evento é obrigatório.");
        }

        if (request.dataInicio() == null){
            throw new IllegalArgumentException("Data do evento é obrigatória.");
        }

        if (request.dataFim() == null){
            throw new IllegalArgumentException("Data do evento é obrigatória.");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        entity.setTime(time);
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setValorSugerido(request.valorSugerido());
        entity.setDataInicio(request.dataInicio());
        entity.setDataFim(request.dataFim());
        entity.setAtivo(true);

        Evento updated = eventoRepository.save(entity);

        log.info("Evento atualizado com sucesso: id={}, nome={}", updated.getId(), updated.getNome());

        return eventoMapper.toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + id));
        eventoRepository.delete(entity);

        log.info("Evento deletado com sucesso: id={}, descricao={}", entity.getId(), entity.getDescricao());
    }
}
