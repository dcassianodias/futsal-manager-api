package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.application.mappers.DespesaMapper;
import com.futsalmanager.domain.entities.Despesa;
import com.futsalmanager.domain.entities.Time;
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

    public DespesaService(DespesaRepository despesaRepository, TimeRepository timeRepository,
                          DespesaMapper despesaMapper) {
        this.despesaRepository = despesaRepository;
        this.timeRepository = timeRepository;
        this.despesaMapper = despesaMapper;
    }

    @Transactional(readOnly = true)
    public DespesaResponse findById(UUID id) {
        Despesa entity = despesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada: " + id));
        return despesaMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> findAll() {
        List<Despesa> list = despesaRepository.findAll();
        return despesaMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> findByTime(UUID timeId){
        List<Despesa> list = despesaRepository.findByTimeIdOrderByMesReferenciaDesc(timeId);
        return despesaMapper.toResponseList(list);
    }

    @Transactional
    public DespesaResponse create(DespesaCreateRequest request){

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        //Validações simples
        if(request.valor() == null || request.valor().signum() <= 0){
            throw new IllegalArgumentException("Valor da despesa deve ser maior que zero.");
        }
        if(request.descricao() == null || request.descricao().isBlank()){
            throw new IllegalArgumentException("Descrição da despesa é obrigatória.");
        }

        Despesa despesa = despesaMapper.toEntity(request);
        despesa.setTime(time);

        Despesa saved = despesaRepository.save(despesa);

        return despesaMapper.toResponse(saved);
    }

    @Transactional
    public DespesaResponse update(UUID id, DespesaUpdateRequest request) {
        Despesa entity = despesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada: " + id));

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        if(request.valor() != null && request.valor().signum() <= 0){
            throw new IllegalArgumentException("Valor da despesa deve ser maior que zero.");
        }
        if(request.descricao() != null && request.descricao().isBlank()){
            throw new IllegalArgumentException("Descrição da despesa é obrigatória.");
        }

        entity.setValor(request.valor());
        entity.setDescricao(request.descricao());
        entity.setMesReferencia(request.mesReferencia());
        entity.setTipoDespesa(request.tipoDespesa());
        entity.setTime(time);

        Despesa updated = despesaRepository.save(entity);
        return despesaMapper.toResponse(updated);
    }

    @Transactional
    public  void delete(UUID id) {
        Despesa entity = despesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada: " + id));
        ;
        despesaRepository.delete(entity);

        log.info("Despesa deletada com sucesso: id={}, descricao={}", entity.getId(), entity.getDescricao());
    }

}
