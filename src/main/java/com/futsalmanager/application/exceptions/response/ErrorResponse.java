package com.futsalmanager.application.exceptions.response;

import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String mensagem,
        List<String> erros,
        LocalDateTime timestamp
) {
    public ErrorResponse(String mensagem){
        this(mensagem, null, LocalDateTime.now());
    }

    public ErrorResponse(String mensagem, List<String> erros){
        this(mensagem, erros, LocalDateTime.now());
    }
}
