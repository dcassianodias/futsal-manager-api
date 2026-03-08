package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.application.mappers.UsuarioMapper;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper usuarioMapper;
    private final TimeRepository timeRepository;

    public UsuarioService(UsuarioRepository repository, UsuarioMapper usuarioMapper, TimeRepository timeRepository) {
        this.repository = repository;
        this.usuarioMapper = usuarioMapper;
        this.timeRepository = timeRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(UUID id) {
        Usuario entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado" + id));

        return usuarioMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioMapper.toResponseList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findByTimeId(UUID timeId) {
        return usuarioMapper.toResponseList(repository.findByTimeId(timeId));
    }

    @Transactional
    public UsuarioResponse create(UsuarioCreateRequest request) {

        if (request.nome() == null || request.nome().isBlank()){
            throw new RuntimeException("O nome do usuáio é obrigatório");
        }
        if (request.email() == null || request.email().isBlank()){
            throw new RuntimeException("O email do usuário é obrigatório");
        }
        if (request.senha() == null || request.senha().isBlank()){
            throw new RuntimeException("A senha do usuário é obrigatória");
        }
        if (request.timeId() == null){
            throw new RuntimeException("O time do usuário é obrigatório");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        Usuario entity = usuarioMapper.toEntity(request);
        entity.setTime(time);
        entity.setPerfil(PerfilUsuario.ATLETA);
        entity.setAtivo(true);

        log.info("Criando usuário: nome={}, email={}", request.nome(), request.email());

        Usuario saved = repository.save(entity);

        return usuarioMapper.toResponse(saved);


    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioUpdateRequest request) {
        Usuario entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado" + id));

        if (request.nome() != null && request.nome().isBlank()){
            throw new RuntimeException("O nome do usuário é obrigatório");
        }
        if (request.email() != null && request.email().isBlank()){
            throw new RuntimeException("O email do usuário é obrigatório");
        }

        if (request.nome() != null){
            entity.setNome(request.nome());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }

        Usuario saved = repository.save(entity);

        log.info("Usuário atualizado com sucesso: id={}, nome={}", saved.getId(), saved.getNome());

        return usuarioMapper.toResponse(saved);

    }

    @Transactional
    public UsuarioResponse delete(UUID id) {
        Usuario entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado" + id));

        UsuarioResponse dto = usuarioMapper.toResponse(entity);

        repository.delete(entity);

        log.info("Usuário deletado com sucesso: id={}, nome={}", entity.getId(), entity.getNome());

        return dto;
    }
}
