package com.futsalmanager.api.controller;

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

import java.util.List;
import java.util.UUID;

/**
 * Identidade do usuário (login, nome, e-mail). Gestão de vínculo com time(s) e
 * perfil (ADMIN/ATLETA) fica em {@link UsuarioTimeController}, sob /api/time/{timeId}/membros.
 */
@RestController
@RequestMapping(value = "/api/usuario/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de identidade do usuário")
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

    @PreAuthorize("@platformOwnerGuard.isOwner()")
    @GetMapping
    @Operation(summary = "Listar todos os usuários ativos (restrito ao dono da plataforma)")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))
    )
    public List<UsuarioResponse> findAll(){
        return service.findAll();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar o próprio perfil (nome, e-mail, senha)")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))
    )
    public ResponseEntity<UsuarioResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid UsuarioUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }
}
