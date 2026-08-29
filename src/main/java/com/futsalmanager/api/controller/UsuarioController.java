package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.application.services.UsuarioService;
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
@RequestMapping(value = "/api/usuario/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@ApiResponseCommon
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))
    )
    public UsuarioResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar todos os usuários ativos")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))
    )
    public List<UsuarioResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    @Operation(summary = "Listar usuários por time")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))
    )
    public List<UsuarioResponse> findByTimeId(@PathVariable UUID timeId){
        return service.findByTimeId(timeId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Criar novo usuário")
    @ApiResponse(
            responseCode = "201",
            description = "Usuário criado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))
    )
    public ResponseEntity<UsuarioResponse> create(@RequestBody @Valid UsuarioCreateRequest request){
        UsuarioResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))
    )
    public ResponseEntity<UsuarioResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid UsuarioUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar usuário (soft delete)")
    @ApiResponse(
            responseCode = "204",
            description = "Usuário desativado"
    )
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar usuário")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário reativado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))
    )
    public ResponseEntity<UsuarioResponse> reativar(@PathVariable UUID id){
        return ResponseEntity.ok(service.reativar(id));
    }
}