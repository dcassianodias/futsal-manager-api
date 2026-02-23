package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.application.mappers.DespesaMapper;
import com.futsalmanager.domain.entities.Despesa;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.DespesaRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
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

    @Transactional
    public Despesa criar(DespesaCreateRequest request){

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

        return despesaRepository.save(despesa);
    }

    @Transactional
    public List<Despesa> listarPorTime(UUID timeId){
        return despesaRepository.findByTimeIdOrderByMesReferenciaDesc(timeId);
    }

}
