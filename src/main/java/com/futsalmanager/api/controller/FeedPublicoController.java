package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.response.FeedPublicoResponse;
import com.futsalmanager.application.services.JogoService;
import com.futsalmanager.openapi.annotations.ApiResponseCommon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/feed/publico", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Feed Público", description = "Feed agregado (sem login) dos times públicos: jogos recentes e artilheiros")
@ApiResponseCommon
public class FeedPublicoController {

    private final JogoService jogoService;

    public FeedPublicoController(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @GetMapping
    @Operation(summary = "Buscar feed agregado dos times públicos (jogos recentes e artilheiros)")
    @ApiResponse(
            responseCode = "200",
            description = "Feed agregado da comunidade",
            content = @Content(schema = @Schema(implementation = FeedPublicoResponse.class))
    )
    public ResponseEntity<FeedPublicoResponse> buscar() {
        return ResponseEntity.ok(jogoService.buscarFeedAgregado());
    }
}
