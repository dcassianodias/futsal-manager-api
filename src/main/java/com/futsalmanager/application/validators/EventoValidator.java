package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.domain.entities.Evento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class EventoValidator {

    public void validarCreate(EventoCreateRequest request) {
        if (request.dataInicio() != null && request.dataFim() != null) {
            validarDatas(request.dataInicio(), request.dataFim());
        }
        validarValor(request.valorSugerido());
    }

    public void validarUpdate(EventoUpdateRequest request) {
        if (request.dataInicio() != null && request.dataFim() != null) {
            validarDatas(request.dataInicio(), request.dataFim());
        }
        validarValor(request.valorSugerido());
    }

    private void validarDatas(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new BusinessException("Data fim não pode ser anterior à data início.");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor != null && valor.signum() < 0) {
            throw new BusinessException("Valor sugerido não pode ser negativo.");
        }
    }

}
