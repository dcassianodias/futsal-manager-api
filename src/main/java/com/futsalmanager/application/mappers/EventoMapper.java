package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.domain.entities.Evento;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventoMapper {

    //Entity -> Response
    @Mapping(target = "timeId", source = "time.id")
    EventoResponse toResponse(Evento entity);

    List<EventoResponse> toResponseList(List<Evento> entities);

    //Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true) // será setado no Service
    @Mapping(target = "ativo", constant = "true") // novo evento é sempre ativo
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Evento toEntity(EventoCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntityFromRequest(EventoUpdateRequest request, @MappingTarget Evento entity);

}
