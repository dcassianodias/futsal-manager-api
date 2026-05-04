package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.domain.entities.Jogo;
import org.mapstruct.*;

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
    @Mapping(target = "statusJogo", constant = "AGENDADO")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "adversario", source = "adversario", qualifiedByName = "normalizeAdversario")
    Jogo toEntity(JogoCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "statusJogo", ignore = true) // ⚠️ regra de negócio
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "adversario", source = "adversario", qualifiedByName = "normalizeAdversario")
    void updateEntityFromRequest(JogoUpdateRequest request, @MappingTarget Jogo entity);

    @Named("normalizeAdversario")
    default String normalizeAdversario(String adversario) {
        if (adversario == null) {
            return null;
        }

        String value = adversario.trim().replaceAll("\\s+", " ");

        if (value.isEmpty()) {
            throw new BusinessException("Adversário não pode ser vazio");
        }

        return value;
    }
}
