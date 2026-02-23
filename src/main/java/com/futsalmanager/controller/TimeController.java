package com.futsalmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/time/v1")
public class TimeController {

    @GetMapping("/{id}")
    public TimeDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping
    public TimeDTO findAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<TimeDTO> create(@RequestBody TimeDTO timeDto){
        TimeDTO createdTime = service.create(timeDto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTime.getId())
                .toUri();

        return ResponseEntity.created(uri).body(createdTime);
    }

    @PutMapping
    public TimeDTO update(@RequestBody TimeDTO timeDto){
        return service.update(timeDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TimeDTO> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
