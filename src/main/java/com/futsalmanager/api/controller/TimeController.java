package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.application.services.TimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time/v1")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping("/{id}")
    public TimeResponse findById(@PathVariable UUID id) {
        return timeService.findById(id);
    }

    @GetMapping
    public List<TimeResponse> findAll() {
        return timeService.findAll();

    }

    @PostMapping
    public ResponseEntity<TimeResponse> create(@RequestBody TimeCreateRequest timeDto){
        TimeResponse createdTime = timeService.create(timeDto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTime.id())
                .toUri();

        return ResponseEntity.created(uri).body(createdTime);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeResponse> update(@PathVariable UUID id, @RequestBody TimeUpdateRequest request){
        return ResponseEntity.ok(timeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        timeService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
