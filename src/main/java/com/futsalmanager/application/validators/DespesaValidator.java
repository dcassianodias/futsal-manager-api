package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.application.validators.common.CommonValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DespesaValidator {

    private final CommonValidator commonValidator;

    public DespesaValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }

    public void validarCreate(DespesaCreateRequest request){
        commonValidator.validarValorPositivo(request.valor(), "Valor da despesa");
        commonValidator.validarTextoObrigatorio(request.descricao(), "Descrição da despesa");
    }

    public void validarUpdate(DespesaUpdateRequest  request) {
        commonValidator.validarValorPositivo(request.valor(), "Valor da despesa");
        commonValidator.validarTextoObrigatorio(request.descricao(), "Descrição da despesa");
    }

}
