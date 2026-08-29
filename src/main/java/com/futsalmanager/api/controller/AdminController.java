package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.response.PlatformStatsResponse;
import com.futsalmanager.application.services.PlatformStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/admin/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin", description = "Endpoints restritos ao dono da plataforma")
public class AdminController {

    private final PlatformStatsService platformStatsService;

    public AdminController(PlatformStatsService platformStatsService) {
        this.platformStatsService = platformStatsService;
    }

    @PreAuthorize("@platformOwnerGuard.isOwner()")
    @GetMapping("/stats")
    @Operation(summary = "Estatísticas globais da plataforma (restrito ao dono)")
    public PlatformStatsResponse stats() {
        return platformStatsService.obter();
    }
}
