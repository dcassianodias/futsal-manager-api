package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.PagamentoCreateRequest;
import com.futsalmanager.api.dto.request.PagamentoUpdateRequest;
import com.futsalmanager.api.dto.response.PagamentoResponse;
import com.futsalmanager.domain.entities.Pagamento;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PagamentoMapper {

    //Enity -> Response
    @Mapping(target = "timeId", source = "time.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "eventoId", expression = "java(entity.getEvento() != null ? entity.getEvento().getId() : null)")
    @Mapping(target = "tipo", source = "tipoPagamento")
    @Mapping(target = "status", source = "statusPagamento")
    PagamentoResponse toResponse(Pagamento entity);

    List<PagamentoResponse> toResponseList(List<Pagamento> list);

    //Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "evento", ignore = true)
    @Mapping(target = "tipoPagamento", source = "tipo")
    @Mapping(target = "statusPagamento", expression = "java(com.futsalmanager.domain.enums.StatusPagamento.PENDENTE)")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Pagamento toEntity(PagamentoCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "evento", ignore = true) // ⚠️ cuidado (explico abaixo)
    @Mapping(target = "tipoPagamento", ignore = true) // ⚠️ regra de negócio
    @Mapping(target = "statusPagamento", ignore = true) // ⚠️ controlado por endpoint
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntityFromRequest(PagamentoUpdateRequest request, @MappingTarget Pagamento entity);

}
