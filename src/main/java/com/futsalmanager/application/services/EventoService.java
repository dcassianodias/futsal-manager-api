package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.EventoMapper;
import com.futsalmanager.application.validators.EventoValidator;
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
    private final EventoValidator validator;

    public EventoService(EventoRepository eventoRepository, TimeRepository timeRepository,
                         EventoMapper eventoMapper, EventoValidator validator) {
        this.eventoRepository = eventoRepository;
        this.timeRepository = timeRepository;
        this.eventoMapper = eventoMapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public EventoResponse findById(UUID id) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));
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
        validator.validarCreate(request);

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + request.timeId()));

        Evento evento = eventoMapper.toEntity(request);
        evento.setTime(time);

        Evento saved = eventoRepository.save(evento);

        log.info("Evento salvo com sucesso: id={}, nome={}", saved.getId(), saved.getNome());

        return eventoMapper.toResponse(saved);
    }

    @Transactional
    public EventoResponse update(UUID id, EventoUpdateRequest request) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        validator.validarUpdate(request);

        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setValorSugerido(request.valorSugerido());
        entity.setDataInicio(request.dataInicio());
        entity.setDataFim(request.dataFim());

        Evento updated = eventoRepository.save(entity);

        log.info("Evento atualizado com sucesso: id={}, nome={}", updated.getId(), updated.getNome());

        return eventoMapper.toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));
        eventoRepository.delete(entity);

        log.info("Evento deletado com sucesso: id={}, descricao={}", entity.getId(), entity.getDescricao());
    }
}
