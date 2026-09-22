package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.response.DespesaAbatimentoResponse;
import com.futsalmanager.domain.entities.DespesaAbatimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DespesaAbatimentoMapper {

    @Mapping(target = "despesaId", source = "despesa.id")
    @Mapping(target = "registradoPorNome", source = "registradoPor.nome")
    DespesaAbatimentoResponse toResponse(DespesaAbatimento entity);

    List<DespesaAbatimentoResponse> toResponseList(List<DespesaAbatimento> list);
}
