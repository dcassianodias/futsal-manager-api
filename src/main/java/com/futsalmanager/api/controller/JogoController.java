package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.services.JogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jogo/v1")
public class JogoController {

    private final JogoService service;

    public JogoController(JogoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public JogoResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }

    @GetMapping
    public List<JogoResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<JogoResponse>> findByTime(@PathVariable UUID timeId){
        List<JogoResponse> list = service.findByTime(timeId);
        if (list.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<JogoResponse> create(@RequestBody JogoCreateRequest request){
        JogoResponse created = service.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogoResponse> update(@PathVariable UUID id, @RequestBody JogoUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
