package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.PagamentoCreateRequest;
import com.futsalmanager.api.dto.request.PagamentoUpdateRequest;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.domain.entities.Evento;
import com.futsalmanager.domain.entities.Pagamento;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.TipoPagamento;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class PagamentoValidator {

    public void validarCreate(PagamentoCreateRequest request, Usuario usuario, Evento evento) {
        if (!usuario.getTime().getId().equals(request.timeId())) {
            throw new BusinessException("Usuário não pertence ao time informado.");
        }

        validarRegrasPorTipo(
                request.tipoPagamento(),
                request.mesReferencia(),
                request.eventoId()
        );

        if (request.tipoPagamento() == TipoPagamento.EVENTO) {

            if (evento == null) {
                throw new BusinessException("Evento deve ser informado.");
            }

            if (!evento.getTime().getId().equals(request.timeId())) {
                throw new BusinessException("Evento não pertence ao time informado.");
            }
        }
    }

    public void validarUpdate(Pagamento entity, PagamentoUpdateRequest request) {
        validarRegrasPorTipo(
                entity.getTipoPagamento(),
                request.mesReferencia(),
                request.eventoId()
        );
    }

    private void validarRegrasPorTipo(TipoPagamento tipo, LocalDate mesReferencia, UUID eventoId) {

        if (tipo == TipoPagamento.MENSALIDADE) {
            if (mesReferencia == null) {
                throw new BusinessException("Mês de referência é obrigatório.");
            }
            if (eventoId != null) {
                throw new BusinessException("Mensalidade não pode ter evento.");
            }
        }

        if (tipo == TipoPagamento.EVENTO) {
            if (eventoId == null) {
                throw new BusinessException("Evento é obrigatório.");
            }
            if (mesReferencia != null) {
                throw new BusinessException("Evento não pode ter mês de referência.");
            }
        }
    }
}
