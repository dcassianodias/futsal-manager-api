package com.futsalmanager.application.validators.common;

import com.futsalmanager.application.exceptions.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CommonValidator {

    public void validarValorPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessException(campo + " deve ser maior que zero.");
        }
    }

    public void validarTextoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new BusinessException(campo + " é obrigatório.");
        }
    }
}
