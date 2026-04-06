package com.futsalmanager.application.exceptions.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Resposta padrão de erro de API")
public record ErrorResponse(

        @Schema(description = "Mensagem principal de erro", example = "Recurso não encontrado")
        String mensagem,

        @Schema(description = "Lista de erros detalhados", example = "['Campo \"nome\" é obrigatório', " +
                "'Campo \"email\" deve ser um endereço de email válido']")
        List<String> erros,

        @Schema(description = "Data/hora do erro", example = "2024-06-01T12:00:00")
        LocalDateTime timestamp
) {
    public ErrorResponse(String mensagem){
        this(mensagem, List.of(), LocalDateTime.now());
    }

    public ErrorResponse(String mensagem, List<String> erros){
        this(mensagem, erros != null ? erros : List.of(), LocalDateTime.now());
    }
}
