package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.EventoMapper;
import com.futsalmanager.application.validators.EventoValidator;
import com.futsalmanager.domain.entities.Evento;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.EventoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
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
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public EventoService(EventoRepository eventoRepository, TimeRepository timeRepository,
                         EventoMapper eventoMapper, EventoValidator validator,
                         AuthenticatedUserProvider authenticatedUserProvider) {
        this.eventoRepository = eventoRepository;
        this.timeRepository = timeRepository;
        this.eventoMapper = eventoMapper;
        this.validator = validator;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public EventoResponse findById(UUID id) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));
        authenticatedUserProvider.validarMembro(entity.getTime().getId());
        return eventoMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> findAll() {
        List<Evento> list = eventoRepository.findByAtivoTrue();
        return eventoMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> findByTime(UUID timeId){
        authenticatedUserProvider.validarMembro(timeId);
        List<Evento> list = eventoRepository.findByTimeIdOrderByDataInicioDesc(timeId);
        return eventoMapper.toResponseList(list);
    }

    @Transactional
    public EventoResponse create(EventoCreateRequest request){
        authenticatedUserProvider.validarAdminDoTime(request.timeId());
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
        authenticatedUserProvider.validarAdminDoTime(entity.getTime().getId());

        validator.validarUpdate(request);

        eventoMapper.updateEntityFromRequest(request, entity);

        Evento updated = eventoRepository.save(entity);

        log.info("Evento atualizado com sucesso: id={}, nome={}", updated.getId(), updated.getNome());

        return eventoMapper.toResponse(updated);
    }

    public void desativar(UUID id) {
        Evento evento = buscarOuErro(id);
        authenticatedUserProvider.validarAdminDoTime(evento.getTime().getId());

        if (!evento.getAtivo()) {
            throw new BusinessException("Evento já está inativo");
        }

        evento.setAtivo(false);

        eventoRepository.save(evento);

        log.info("Evento desativado: id={}, nome={}", evento.getId(), evento.getNome());

    }

    public void ativar(UUID id) {
        Evento evento = buscarOuErro(id);
        authenticatedUserProvider.validarAdminDoTime(evento.getTime().getId());

        if (evento.getAtivo()) {
            throw new BusinessException("Evento já está ativo");
        }

        evento.setAtivo(true);

        eventoRepository.save(evento);

        log.info("Evento reativado: id={}, nome={}", evento.getId(), evento.getNome());
    }

    private Evento buscarOuErro(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));
    }


}
