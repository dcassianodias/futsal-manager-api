package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.application.mappers.DespesaMapper;
import com.futsalmanager.application.services.DespesaService;
import com.futsalmanager.domain.entities.Despesa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/despesa/v1")
public class DespesaController {

    private final DespesaService service;
    private final DespesaMapper mapper;

    public DespesaController(DespesaService service, DespesaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<DespesaResponse> create(@RequestBody DespesaCreateRequest request){
        Despesa saved = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    public ResponseEntity<List<DespesaResponse>> findByTime(@PathVariable UUID timeId){
        List<DespesaResponse> response = service.listarPorTime(timeId).stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}
