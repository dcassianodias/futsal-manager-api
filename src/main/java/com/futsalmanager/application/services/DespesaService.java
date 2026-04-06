package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.DespesaMapper;
import com.futsalmanager.application.validators.DespesaValidator;
import com.futsalmanager.domain.entities.Despesa;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.infrastructure.repositories.DespesaRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final TimeRepository timeRepository;
    private final DespesaMapper despesaMapper;
    private final DespesaValidator validator;

    public DespesaService(DespesaRepository despesaRepository, TimeRepository timeRepository,
                          DespesaMapper despesaMapper, DespesaValidator validator) {
        this.despesaRepository = despesaRepository;
        this.timeRepository = timeRepository;
        this.despesaMapper = despesaMapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public DespesaResponse findById(UUID id) {
        return despesaMapper.toResponse(despesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + id)));
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> findAll() {
        return despesaMapper.toResponseList(despesaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> findByTime(UUID timeId){
        return despesaMapper.toResponseList(despesaRepository.findByTimeIdOrderByMesReferenciaDesc(timeId));
    }

    @Transactional
    public DespesaResponse create(DespesaCreateRequest request){
        validator.validarCreate(request);

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + request.timeId()));

        Despesa despesa = despesaMapper.toEntity(request);
        despesa.setTime(time);

        Despesa saved = despesaRepository.save(despesa);

        log.info("Despesa criada: id={}, valor={}", saved.getId(), saved.getValor());

        return despesaMapper.toResponse(saved);
    }

    @Transactional
    public DespesaResponse update(UUID id, DespesaUpdateRequest request) {
        Despesa entity = despesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + id));

        validator.validarUpdate(request);

        despesaMapper.updateEntityFromRequest(request, entity);

        return despesaMapper.toResponse(despesaRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        Despesa entity = despesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + id));

        despesaRepository.delete(entity);

        log.info("Despesa deletada com sucesso: id={}, descricao={}", entity.getId(), entity.getDescricao());
    }


}
