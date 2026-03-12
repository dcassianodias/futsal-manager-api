package com.futsalmanager.application.mappers;

import com.futsalmanager.api.dto.request.PagamentoCreateRequest;
import com.futsalmanager.api.dto.response.PagamentoResponse;
import com.futsalmanager.domain.entities.Pagamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PagamentoMapper {

    //Enity -> Response
    @Mapping(target = "timeId", source = "time.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "eventoId", source = "evento.id")
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
    @Mapping(target = "statusPagamento", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Pagamento toEntity(PagamentoCreateRequest request);

}
