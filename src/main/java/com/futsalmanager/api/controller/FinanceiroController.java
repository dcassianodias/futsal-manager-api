package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.response.ResumoFinanceiroResponse;
import com.futsalmanager.application.services.FinanceiroService;
import com.futsalmanager.openapi.annotations.ApiResponseCommon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/financeiro/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Financeiro", description = "Painel de caixa do time — restrito a admins")
@ApiResponseCommon
public class FinanceiroController {

    private final FinanceiroService service;

    public FinanceiroController(FinanceiroService service) {
        this.service = service;
    }

    @GetMapping("/time/{timeId}/resumo")
    @Operation(
            summary = "Obter resumo financeiro do time",
            description = "Retorna o saldo consolidado (entradas de pagamentos menos saídas de despesas), pendências e últimos lançamentos. Restrito a admins do time."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Resumo financeiro",
            content = @Content(schema = @Schema(implementation = ResumoFinanceiroResponse.class))
    )
    public ResumoFinanceiroResponse obterResumo(@PathVariable UUID timeId) {
        return service.obterResumo(timeId);
    }
}
