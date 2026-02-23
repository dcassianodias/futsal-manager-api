package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.domain.entities.Time;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeMapper {

    //Entity -> Response
    TimeResponse toResponse(Time entity);
    List<TimeResponse> toResponseList(List<Time> entities);

    //Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Time toEntity(TimeCreateRequest request);

    //Atualiza a entity existente(não cria outra)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntityFromRequest(TimeUpdateRequest request, @MappingTarget Time entity);

}
