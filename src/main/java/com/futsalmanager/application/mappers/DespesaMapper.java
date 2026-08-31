package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.domain.entities.Despesa;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DespesaMapper {

    //Entity -> Response
    @Mapping(target = "timeId", source = "time.id")
    DespesaResponse toResponse(Despesa entity);
    List<DespesaResponse> toResponseList(List<Despesa> list);

    //Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "status", constant = "PENDENTE")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Despesa toEntity(DespesaCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntityFromRequest(DespesaUpdateRequest request, @MappingTarget Despesa entity);

}
