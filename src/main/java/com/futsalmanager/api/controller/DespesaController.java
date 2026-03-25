package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.application.services.DespesaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/despesa/v1")
public class DespesaController {

    private final DespesaService service;

    public DespesaController(DespesaService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public DespesaResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @GetMapping
    public List<DespesaResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<DespesaResponse>> findByTime(@PathVariable UUID timeId){
        List<DespesaResponse> list = service.findByTime(timeId);

        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<DespesaResponse> create(@RequestBody @Valid DespesaCreateRequest request){
        DespesaResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespesaResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid DespesaUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
