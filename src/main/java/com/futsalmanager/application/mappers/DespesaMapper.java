package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.domain.entities.Despesa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DespesaMapper {

    //Entity -> Response
    @Mapping(target = "timeId", source = "time.id")
    @Mapping(target = "tipo", source = "tipoDespesa")
    DespesaResponse toResponse(Despesa entity);
    List<DespesaResponse> toResponseList(List<Despesa> list);

    //Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true) // será setado no Service
    @Mapping(target = "tipoDespesa", source = "tipo")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    Despesa toEntity(DespesaCreateRequest request);
}
