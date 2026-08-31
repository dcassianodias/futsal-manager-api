package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.AddMembroRequest;
import com.futsalmanager.api.dto.request.AlterarPerfilMembroRequest;
import com.futsalmanager.api.dto.response.MembroResponse;
import com.futsalmanager.application.services.UsuarioTimeService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/time/{timeId}/membros", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Membros", description = "Endpoints para gerenciamento do vínculo de jogadores/admins com um time")
@ApiResponseCommon
public class UsuarioTimeController {

    private final UsuarioTimeService service;

    public UsuarioTimeController(UsuarioTimeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar membros ativos do time")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de membros do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MembroResponse.class)))
    )
    public List<MembroResponse> findByTimeId(@PathVariable UUID timeId) {
        return service.findByTimeId(timeId);
    }

    @PostMapping
    @Operation(summary = "Adicionar membro ao time (reaproveita conta existente pelo email, ou cria uma nova)")
    @ApiResponse(
            responseCode = "201",
            description = "Membro adicionado",
            content = @Content(schema = @Schema(implementation = MembroResponse.class))
    )
    public ResponseEntity<MembroResponse> addMembro(@PathVariable UUID timeId,
                                                     @RequestBody @Valid AddMembroRequest request) {
        return ResponseEntity.status(201).body(service.addMembro(timeId, request));
    }

    @PatchMapping("/{membroId}/perfil")
    @Operation(summary = "Alterar o perfil (ADMIN/ATLETA) de um membro do time")
    @ApiResponse(
            responseCode = "200",
            description = "Perfil alterado",
            content = @Content(schema = @Schema(implementation = MembroResponse.class))
    )
    public ResponseEntity<MembroResponse> alterarPerfil(@PathVariable UUID timeId, @PathVariable UUID membroId,
                                                         @RequestBody @Valid AlterarPerfilMembroRequest request) {
        return ResponseEntity.ok(service.alterarPerfil(timeId, membroId, request));
    }

    @DeleteMapping("/{membroId}")
    @Operation(summary = "Remover membro do time (soft delete do vínculo)")
    @ApiResponse(responseCode = "204", description = "Membro removido")
    public ResponseEntity<Void> removerMembro(@PathVariable UUID timeId, @PathVariable UUID membroId) {
        service.removerMembro(timeId, membroId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{membroId}/reativar")
    @Operation(summary = "Reativar vínculo de um membro removido anteriormente")
    @ApiResponse(
            responseCode = "200",
            description = "Membro reativado",
            content = @Content(schema = @Schema(implementation = MembroResponse.class))
    )
    public ResponseEntity<MembroResponse> reativarMembro(@PathVariable UUID timeId, @PathVariable UUID membroId) {
        return ResponseEntity.ok(service.reativarMembro(timeId, membroId));
    }
}
