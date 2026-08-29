package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.domain.entities.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // Entity -> Response
    @Mapping(target = "timeId", source = "time.id")
    UsuarioResponse toResponse(Usuario entity);

    List<UsuarioResponse> toResponseList(List<Usuario> entities);

    // Create -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "senha", ignore = true)
    @Mapping(
            target = "email",
            expression = "java(request.email().trim().toLowerCase())"
    )
    @Mapping(
            target = "perfil",
            expression = "java(request.perfil() != null ? request.perfil() : com.futsalmanager.domain.enums.PerfilUsuario.ATLETA)"
    )
    Usuario toEntity(UsuarioCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void updateEntityFromRequest(UsuarioUpdateRequest request, @MappingTarget Usuario entity);

}
