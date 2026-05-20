package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.FinalizarJogoRequest;
import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.services.JogoService;
import com.futsalmanager.openapi.annotations.ApiResponseCommon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/jogo/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Jogos", description = "Endpoints para gerenciamento de jogos")
@ApiResponseCommon
public class JogoController {

    private final JogoService service;

    public JogoController(JogoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar jogo por ID")
    @ApiResponse(
            responseCode = "200",
            description = "Jogo encontrado",
            content = @Content(schema = @Schema(implementation = JogoResponse.class))
    )
    public JogoResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos os jogos")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de jogos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = JogoResponse.class)))
    )
    public List<JogoResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    @Operation(summary = "Listar jogos por time")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de jogos do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = JogoResponse.class)))
    )
    public ResponseEntity<List<JogoResponse>> findByTime(@PathVariable UUID timeId){
        List<JogoResponse> list = service.findByTime(timeId);
        return ResponseEntity.ok(list); // 🔥 sempre 200
    }

    @PostMapping
    @Operation(summary = "Criar novo jogo")
    @ApiResponse(
            responseCode = "201",
            description = "Jogo criado",
            content = @Content(schema = @Schema(implementation = JogoResponse.class))
    )
    public ResponseEntity<JogoResponse> create(@RequestBody @Valid JogoCreateRequest request){
        JogoResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar jogo")
    @ApiResponse(
            responseCode = "200",
            description = "Jogo atualizado",
            content = @Content(schema = @Schema(implementation = JogoResponse.class))
    )
    public ResponseEntity<JogoResponse> update(@PathVariable UUID id,
                                               @RequestBody @Valid JogoUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar jogo com placar e artilheiros")
    @ApiResponse(
            responseCode = "200",
            description = "Jogo finalizado",
            content = @Content(schema = @Schema(implementation = JogoResponse.class))
    )
    public ResponseEntity<JogoResponse> finalizar(@PathVariable UUID id,
                                                  @RequestBody @Valid FinalizarJogoRequest request) {
        return ResponseEntity.ok(service.finalizar(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar jogo")
    @ApiResponse(
            responseCode = "200",
            description = "Jogo cancelado",
            content = @Content(schema = @Schema(implementation = JogoResponse.class))
    )
    public ResponseEntity<JogoResponse> cancelar(@PathVariable UUID id){
        return ResponseEntity.ok(service.cancelar(id));
    }
}