package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.domain.entities.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    //Entity -> Response
    @Mapping(target = "timeId", source = "time.id")
    UsuarioResponse toResponse(Usuario entity);

    List<UsuarioResponse> toResponseList(List<Usuario> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "email", expression = "java(request.email().toLowerCase())")
    @Mapping(target = "perfil", constant = "ATLETA")
    Usuario toEntity(UsuarioCreateRequest request);



}
