package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.application.services.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evento/v1")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public EventoResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @GetMapping
    public List<EventoResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<EventoResponse>> findByTime(@PathVariable UUID timeId){
        List<EventoResponse> list = service.findByTime(timeId);
        if (list.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<EventoResponse> create(@RequestBody @Valid EventoCreateRequest request){
        EventoResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> update(@PathVariable UUID id, @RequestBody @Valid EventoUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
