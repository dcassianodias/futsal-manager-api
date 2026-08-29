package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.application.services.EventoService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/evento/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Eventos", description = "Endpoints para gerenciamento de eventos")
@ApiResponseCommon
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Encontrar evento por ID",
            description = "Retorna um evento específico pelo seu ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Evento encontrado",
            content = @Content(schema = @Schema(implementation = EventoResponse.class))
    )
    public EventoResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(
            summary = "Encontrar todos os eventos",
            description = "Retorna uma lista de todos os eventos ativos."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de eventos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EventoResponse.class)))
    )
    public List<EventoResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    @Operation(
            summary = "Listar eventos por time",
            description = "Retorna uma lista de eventos organizados por um time específico."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de eventos do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EventoResponse.class)))
    )
    public ResponseEntity<List<EventoResponse>> findByTime(@PathVariable UUID timeId){
        List<EventoResponse> list = service.findByTime(timeId);
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Criar novo evento",
            description = "Cria um novo evento com as informações fornecidas."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Evento criado",
            content = @Content(schema = @Schema(implementation = EventoResponse.class))
    )
    public ResponseEntity<EventoResponse> create(@RequestBody @Valid EventoCreateRequest request){
        EventoResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Atualiza as informações de um evento existente pelo seu ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Evento atualizado",
            content = @Content(schema = @Schema(implementation = EventoResponse.class))
    )
    public ResponseEntity<EventoResponse> update(@PathVariable UUID id, @RequestBody @Valid EventoUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desativar")
    @Operation(
            summary = "Desativar evento",
            description = "Desativa um evento existente pelo seu ID, tornando-o inativo."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Evento desativado"
    )
    public ResponseEntity<Void> desativar(@PathVariable UUID id){
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/ativar")
    @Operation(
            summary = "Ativar evento",
            description = "Ativa um evento inativo pelo seu ID, tornando-o ativo novamente."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Evento ativado"
    )
    public ResponseEntity<Void> ativar(@PathVariable UUID id){
        service.ativar(id);
        return ResponseEntity.noContent().build();
    }

}
