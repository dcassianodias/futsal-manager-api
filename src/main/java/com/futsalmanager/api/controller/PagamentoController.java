package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.*;
import com.futsalmanager.api.dto.response.GerarMensalidadeResponse;
import com.futsalmanager.api.dto.response.PagamentoResponse;
import com.futsalmanager.application.services.PagamentoService;
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
@RequestMapping(value = "/api/pagamento/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pagamentos", description = "Endpoints para gerenciamento de pagamentos")
@ApiResponseCommon
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    @ApiResponse(
            responseCode = "200",
            description = "Pagamento encontrado",
            content = @Content(schema = @Schema(implementation = PagamentoResponse.class))
    )
    public PagamentoResponse findById(@PathVariable UUID id){
        return pagamentoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar todos os pagamentos")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pagamentos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagamentoResponse.class)))
    )
    public List<PagamentoResponse> findAll(){
        return pagamentoService.findAll();
    }

    @GetMapping("/time/{timeId}")
    @Operation(summary = "Listar pagamentos por time")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pagamentos do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagamentoResponse.class)))
    )
    public ResponseEntity<List<PagamentoResponse>> findByTime(@PathVariable UUID timeId){
        return ResponseEntity.ok(pagamentoService.findByTime(timeId));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar histórico de pagamentos por usuário")
    @ApiResponse(
            responseCode = "200",
            description = "Histórico de pagamentos do usuário",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagamentoResponse.class)))
    )
    public ResponseEntity<List<PagamentoResponse>> findByUsuario(@PathVariable UUID usuarioId){
        return ResponseEntity.ok(pagamentoService.findByUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/pendentes")
    @Operation(summary = "Listar pagamentos pendentes por usuário")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pagamentos pendentes do usuário",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagamentoResponse.class)))
    )
    public ResponseEntity<List<PagamentoResponse>> findPendenteByUsuario(@PathVariable UUID usuarioId){
        return ResponseEntity.ok(pagamentoService.findPendentesByUsuario(usuarioId));
    }

    @GetMapping("/time/{timeId}/pendentes")
    @Operation(summary = "Listar pagamentos pendentes por time")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pagamentos pendentes do time",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagamentoResponse.class)))
    )
    public ResponseEntity<List<PagamentoResponse>> findPendenteByTime(@PathVariable UUID timeId){
        return ResponseEntity.ok(pagamentoService.findPendentesByTime(timeId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Criar novo pagamento")
    @ApiResponse(
            responseCode = "201",
            description = "Pagamento criado",
            content = @Content(schema = @Schema(implementation = PagamentoResponse.class))
    )
    public ResponseEntity<PagamentoResponse> create(@RequestBody @Valid PagamentoCreateRequest request){
        PagamentoResponse created = pagamentoService.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar pagamento")
    @ApiResponse(
            responseCode = "200",
            description = "Pagamento atualizado",
            content = @Content(schema = @Schema(implementation = PagamentoResponse.class))
    )
    public ResponseEntity<PagamentoResponse> update(@PathVariable UUID id,
                                                    @RequestBody @Valid PagamentoUpdateRequest request){
        return ResponseEntity.ok(pagamentoService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Marcar pagamento como pago")
    @ApiResponse(
            responseCode = "200",
            description = "Pagamento marcado como pago",
            content = @Content(schema = @Schema(implementation = PagamentoResponse.class))
    )
    public ResponseEntity<PagamentoResponse> marcarComoPago(@PathVariable UUID id){
        return ResponseEntity.ok(pagamentoService.marcarComoPago(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/mensalidades/gerar")
    @Operation(summary = "Gerar mensalidades para o time")
    @ApiResponse(
            responseCode = "200",
            description = "Mensalidades geradas",
            content = @Content(schema = @Schema(implementation = GerarMensalidadeResponse.class))
    )
    public ResponseEntity<GerarMensalidadeResponse> gerarMensalidades(@RequestBody @Valid GerarMensalidadeRequest request) {
        return ResponseEntity.ok(pagamentoService.gerarMensalidades(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pagamento")
    @ApiResponse(
            responseCode = "200",
            description = "Pagamento cancelado",
            content = @Content(schema = @Schema(implementation = PagamentoResponse.class))
    )
    public ResponseEntity<PagamentoResponse> cancelar(@PathVariable UUID id){
        return ResponseEntity.ok(pagamentoService.cancelar(id));
    }
}