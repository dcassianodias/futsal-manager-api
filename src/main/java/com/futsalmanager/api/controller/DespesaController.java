package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.application.services.DespesaService;
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
@RequestMapping(value = "/api/despesa/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Despesas", description = "Endpoints para gerenciamento de despesas")
@ApiResponseCommon
public class DespesaController {

    private final DespesaService service;

    public DespesaController(DespesaService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Encontrar despesa por ID",
            description = "Retorna uma despesa específica pelo seu ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Despesa encontrada",
            content = @Content(schema = @Schema(implementation = DespesaResponse.class))
    )
    public DespesaResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @PreAuthorize("@platformOwnerGuard.isOwner()")
    @GetMapping
    @Operation(
            summary = "Encontrar todas as despesas (restrito ao dono da plataforma)",
            description = "Retorna uma lista de todas as despesas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de despesas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DespesaResponse.class)))
    )
    public List<DespesaResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    @Operation(
            summary = "Listar despesas por time",
            description = "Retorna uma lista de despesas associadas a um time específico."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de despesas do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DespesaResponse.class)))
    )
    public ResponseEntity<List<DespesaResponse>> findByTime(@PathVariable UUID timeId){
        List<DespesaResponse> list = service.findByTime(timeId);

        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Operation(
            summary = "Criar nova despesa",
            description = "Cria uma nova despesa associada a um time. Retorna a despesa criada com seu ID."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Despesa criada",
            content = @Content(schema = @Schema(implementation = DespesaResponse.class))
    )
    public ResponseEntity<DespesaResponse> create(@RequestBody @Valid DespesaCreateRequest request){
        DespesaResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualizar despesa",
            description = "Atualiza os dados de uma despesa existente. Retorna a despesa atualizada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Despesa atualizada",
            content = @Content(schema = @Schema(implementation = DespesaResponse.class))
    )
    public ResponseEntity<DespesaResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid DespesaUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/pagar")
    @Operation(
            summary = "Marcar despesa como paga",
            description = "Marca uma despesa pendente como paga. Retorna a despesa atualizada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Despesa marcada como paga",
            content = @Content(schema = @Schema(implementation = DespesaResponse.class))
    )
    public ResponseEntity<DespesaResponse> marcarComoPago(@PathVariable UUID id){
        return ResponseEntity.ok(service.marcarComoPago(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir despesa",
            description = "Exclui uma despesa existente pelo seu ID. Retorna status 204 No Content em caso de sucesso."
    )
    @ApiResponse(responseCode = "204", description = "Despesa excluída")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
