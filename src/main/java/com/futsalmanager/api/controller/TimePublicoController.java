package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.response.TimePublicoResponse;
import com.futsalmanager.application.services.JogoService;
import com.futsalmanager.application.services.TimeService;
import com.futsalmanager.openapi.annotations.ApiResponseCommon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/time/publico", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Time Público", description = "Feed público (sem login) de times que optaram por ficar visíveis")
@ApiResponseCommon
public class TimePublicoController {

    private final JogoService jogoService;
    private final TimeService timeService;

    public TimePublicoController(JogoService jogoService, TimeService timeService) {
        this.jogoService = jogoService;
        this.timeService = timeService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar feed público de um time (nome, próximo jogo, últimos resultados)")
    @ApiResponse(
            responseCode = "200",
            description = "Feed público do time",
            content = @Content(schema = @Schema(implementation = TimePublicoResponse.class))
    )
    public ResponseEntity<TimePublicoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(jogoService.buscarFeedPublico(id));
    }

    @GetMapping(value = "/{id}/logo")
    @Operation(summary = "Buscar o logo do time (imagem)")
    public ResponseEntity<byte[]> logo(@PathVariable UUID id) {
        TimeService.Imagem imagem = timeService.buscarLogo(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.contentType()))
                .body(imagem.dados());
    }
}
