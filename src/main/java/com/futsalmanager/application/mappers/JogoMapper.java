package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.domain.entities.Jogo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JogoMapper {

    //Entity -> Response
    @Mapping(target = "timeId", source = "time.id")
    @Mapping(target = "status", source = "statusJogo")
    JogoResponse toResponse(Jogo entity);

    List<JogoResponse> toResponseList(List<Jogo> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "statusJogo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Jogo toEntity(JogoCreateRequest request);
}
