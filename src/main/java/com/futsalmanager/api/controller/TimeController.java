package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.application.services.TimeService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/time/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Times", description = "Endpoints para gerenciamento de times")
@ApiResponseCommon
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar time por ID")
    @ApiResponse(
            responseCode = "200",
            description = "Time encontrado",
            content = @Content(schema = @Schema(implementation = TimeResponse.class))
    )
    public TimeResponse findById(@PathVariable UUID id) {
        return timeService.findById(id);
    }

    @PreAuthorize("@platformOwnerGuard.isOwner()")
    @GetMapping
    @Operation(summary = "Listar todos os times (restrito ao dono da plataforma)")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de times",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeResponse.class)))
    )
    public List<TimeResponse> findAll() {
        return timeService.findAll();
    }

    @PostMapping
    @Operation(summary = "Criar novo time")
    @ApiResponse(
            responseCode = "201",
            description = "Time criado",
            content = @Content(schema = @Schema(implementation = TimeResponse.class))
    )
    public ResponseEntity<TimeResponse> create(@RequestBody @Valid TimeCreateRequest request){
        TimeResponse created = timeService.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar time")
    @ApiResponse(
            responseCode = "200",
            description = "Time atualizado",
            content = @Content(schema = @Schema(implementation = TimeResponse.class))
    )
    public ResponseEntity<TimeResponse> update(@PathVariable UUID id,
                                               @RequestBody @Valid TimeUpdateRequest request){
        return ResponseEntity.ok(timeService.update(id, request));
    }

    @PreAuthorize("@platformOwnerGuard.isOwner()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir time (restrito ao dono da plataforma)")
    @ApiResponse(
            responseCode = "204",
            description = "Time excluído"
    )
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        timeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar time")
    @ApiResponse(
            responseCode = "204",
            description = "Time desativado"
    )
    public ResponseEntity<Void> desativar(@PathVariable UUID id){
        timeService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar time")
    @ApiResponse(
            responseCode = "204",
            description = "Time ativado"
    )
    public ResponseEntity<Void> ativar(@PathVariable UUID id){
        timeService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/tornar-publico")
    @Operation(summary = "Tornar o time público (visível na página pública sem login)")
    @ApiResponse(
            responseCode = "204",
            description = "Time tornado público"
    )
    public ResponseEntity<Void> tornarPublico(@PathVariable UUID id){
        timeService.tornarPublico(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/tornar-privado")
    @Operation(summary = "Tornar o time privado (remove a página pública)")
    @ApiResponse(
            responseCode = "204",
            description = "Time tornado privado"
    )
    public ResponseEntity<Void> tornarPrivado(@PathVariable UUID id){
        timeService.tornarPrivado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/regenerar-codigo")
    @Operation(summary = "Gerar um novo código de convite, invalidando o link anterior")
    @ApiResponse(
            responseCode = "200",
            description = "Código regenerado",
            content = @Content(schema = @Schema(implementation = TimeResponse.class))
    )
    public ResponseEntity<TimeResponse> regenerarCodigo(@PathVariable UUID id){
        return ResponseEntity.ok(timeService.regenerarCodigo(id));
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar o logo do time (PNG, JPEG ou WEBP, até 2MB)")
    @ApiResponse(responseCode = "204", description = "Logo atualizado")
    public ResponseEntity<Void> atualizarLogo(@PathVariable UUID id, @RequestParam("arquivo") MultipartFile arquivo){
        timeService.atualizarLogo(id, arquivo);
        return ResponseEntity.noContent().build();
    }

}