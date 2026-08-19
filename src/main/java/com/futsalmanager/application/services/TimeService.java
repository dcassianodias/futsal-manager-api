package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.TimeMapper;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TimeService {

    private final TimeRepository repository;
    private final TimeMapper timeMapper;

    public TimeService(TimeRepository repository, TimeMapper timeMapper) {
        this.repository = repository;
        this.timeMapper = timeMapper;
    }

    @Transactional(readOnly = true)
    public TimeResponse findById(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        return timeMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<TimeResponse> findAll() {
        List<Time> list = repository.findAll();
        return timeMapper.toResponseList(list);
    }

    @Transactional
    public TimeResponse create(TimeCreateRequest request) {

        Time entity = timeMapper.toEntity(request);
        entity.setCodigo(gerarCodigo(entity.getNome()));

        Time saved = repository.save(entity);

        log.info("Time criado: id={}, nome={}", saved.getId(), saved.getNome());

        return timeMapper.toResponse(saved);
    }

    @Transactional
    public TimeResponse update(UUID id, TimeUpdateRequest request) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        timeMapper.updateEntityFromRequest(request, entity);

        Time saved = repository.save(entity);

        log.info("Time atualizado: id={}, nome={}", saved.getId(), saved.getNome());

        return timeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado" + id));

        repository.delete(entity);
        log.info("Time deletado: id={}, nome={}", entity.getId(), entity.getNome());

    }

    @Transactional
    public void desativar(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!entity.getAtivo()) {
            throw new BusinessException("Time já está inativo");
        }

        entity.setAtivo(false);

        repository.save(entity);

        log.info("Time desativado: id={}, nome={}", entity.getId(), entity.getNome());
    }

    @Transactional
    public void ativar(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (entity.getAtivo()) {
            throw new BusinessException("Time já está ativo");
        }

        entity.setAtivo(true);

        repository.save(entity);

        log.info("Time reativado: id={}, nome={}", entity.getId(), entity.getNome());
    }

    private String gerarCodigo(String nome) {
        return nome.substring(0, 3).toUpperCase() + "-" + System.currentTimeMillis();
    }

}
