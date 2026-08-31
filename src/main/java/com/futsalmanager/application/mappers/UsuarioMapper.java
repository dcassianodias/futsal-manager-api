package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.domain.entities.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario entity);

    List<UsuarioResponse> toResponseList(List<Usuario> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void updateEntityFromRequest(UsuarioUpdateRequest request, @MappingTarget Usuario entity);

}
