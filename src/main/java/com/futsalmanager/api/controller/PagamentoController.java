package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.*;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.api.dto.response.GerarMensalidadeResponse;
import com.futsalmanager.api.dto.response.PagamentoResponse;
import com.futsalmanager.application.services.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagamento/v1")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/{id}")
    public PagamentoResponse findById(@PathVariable UUID id){
        return pagamentoService.findById(id);
    }

    @GetMapping
    public List<PagamentoResponse> findAll(){
        return pagamentoService.findAll();
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<PagamentoResponse>> findByTime(@PathVariable UUID timeId){
        List<PagamentoResponse> list = pagamentoService.findByTime(timeId);
        if (list.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/usuario/{usuarioId}/pendentes")
    public ResponseEntity<List<PagamentoResponse>> findPendenteByUsuario(@PathVariable UUID usuarioId){
        List<PagamentoResponse> list = pagamentoService.findPendentesByUsuario(usuarioId);
        if (list.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/time/{timeId}/pendentes")
    public ResponseEntity<List<PagamentoResponse>> findPendenteByTime(@PathVariable UUID timeId){
        List<PagamentoResponse> list = pagamentoService.findPendentesByTime(timeId);
        if (list.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> create(@RequestBody PagamentoCreateRequest request){
        PagamentoResponse created = pagamentoService.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagamentoResponse> update(@PathVariable UUID id, @RequestBody PagamentoUpdateRequest request){
        return ResponseEntity.ok(pagamentoService.update(id, request));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<PagamentoResponse> marcarComoPago(@PathVariable UUID id){
        return ResponseEntity.ok(pagamentoService.marcarComoPago(id));
    }

    @PostMapping("/mensalidades/gerar")
    public ResponseEntity<GerarMensalidadeResponse> gerarMensalidades(@RequestBody GerarMensalidadeRequest request) {
        return ResponseEntity.ok(pagamentoService.gerarMensalidades(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        pagamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
