package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.ConviteRegistrarRequest;
import com.futsalmanager.api.dto.response.ConviteInfoResponse;
import com.futsalmanager.api.dto.response.LoginResponse;
import com.futsalmanager.application.services.AuthService;
import com.futsalmanager.openapi.annotations.ApiResponseCommon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/time/convite", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Convite", description = "Cadastro de atleta via link de convite de um time (sem login)")
@ApiResponseCommon
public class ConviteController {

    private final AuthService authService;

    public ConviteController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Buscar dados do time a partir do código do convite")
    @ApiResponse(
            responseCode = "200",
            description = "Convite válido",
            content = @Content(schema = @Schema(implementation = ConviteInfoResponse.class))
    )
    public ResponseEntity<ConviteInfoResponse> buscar(@PathVariable String codigo) {
        return ResponseEntity.ok(authService.buscarConvite(codigo));
    }

    @PostMapping("/{codigo}/registrar")
    @Operation(summary = "Cadastrar-se como atleta no time do convite")
    @ApiResponse(
            responseCode = "201",
            description = "Cadastro realizado e já autenticado no time",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    public ResponseEntity<LoginResponse> registrar(@PathVariable String codigo,
                                                    @RequestBody @Valid ConviteRegistrarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarViaConvite(codigo, request));
    }
}
