package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.UsuarioMapper;
import com.futsalmanager.application.validators.UsuarioValidator;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UsuarioValidator validator;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UsuarioService(UsuarioRepository repository, UsuarioMapper usuarioMapper, TimeRepository timeRepository, UsuarioValidator validator, PasswordEncoder passwordEncoder, AuthenticatedUserProvider authenticatedUserProvider) {
        this.repository = repository;
        this.usuarioMapper = usuarioMapper;
        this.timeRepository = timeRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(UUID id) {
        Usuario entity = buscarAtivoOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());
        return usuarioMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioMapper.toResponseList(repository.findByAtivoTrue());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findByTimeId(UUID timeId) {
        authenticatedUserProvider.validarAcessoAoTime(timeId);
        return usuarioMapper.toResponseList(repository.findByTimeIdAndAtivoTrue(timeId));
    }

    @Transactional
    public UsuarioResponse create(UsuarioCreateRequest request) {
        authenticatedUserProvider.validarAcessoAoTime(request.timeId());
        validator.validarCreate(request);

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + request.timeId()));

        String email = request.email().toLowerCase();

        var existenteOpt = repository.findByTimeIdAndEmail(request.timeId(), email);

        if (existenteOpt.isPresent()) {
            Usuario existente = existenteOpt.get();

            if (existente.isAtivo()) {
                throw new BusinessException("Email já cadastrado para este time");
            }

            existente.setAtivo(true);
            existente.setNome(request.nome());
            existente.setSenha(passwordEncoder.encode(request.senha()));

            Usuario saved = repository.save(existente);

            log.info("Usuário reativado automaticamente: id={}, email={}", saved.getId(), saved.getEmail());

            return usuarioMapper.toResponse(saved);
        }

        Usuario entity = usuarioMapper.toEntity(request);
        entity.setTime(time);
        entity.setEmail(email);
        entity.setSenha(passwordEncoder.encode(request.senha()));

        Usuario saved = repository.save(entity);

        log.info("Usuário criado: id={}, email={}, perfil={}", saved.getId(), saved.getEmail(), saved.getPerfil());

        return usuarioMapper.toResponse(saved);
    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioUpdateRequest request) {
        Usuario entity = buscarAtivoOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());

        validator.validarUpdate(entity, request);

        usuarioMapper.updateEntityFromRequest(request, entity);

        if (request.email() != null){
            entity.setEmail(request.email().trim().toLowerCase());
        }

        if (request.senha() != null && !request.senha().isBlank()) {
            entity.setSenha(passwordEncoder.encode(request.senha()));
        }

        Usuario saved = repository.save(entity);

        log.info("Usuário atualizado com sucesso: id={}, nome={}", saved.getId(), saved.getNome());

        return usuarioMapper.toResponse(saved);

    }

    @Transactional
    public void delete(UUID id) {
        Usuario entity = buscarAtivoOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());

        entity.setAtivo(false);

        repository.save(entity);

        log.info("Usuário deletado com sucesso: id={}, nome={}", entity.getId(), entity.getNome());
    }

    @Transactional
    public UsuarioResponse reativar(UUID id) {

        Usuario entity = buscarOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());

        if (entity.isAtivo()) {
            throw new BusinessException("Usuário já está ativo");
        }

        entity.setAtivo(true);

        Usuario saved = repository.save(entity);

        log.info("Usuário reativado: id={}, nome={}", saved.getId(), saved.getNome());

        return usuarioMapper.toResponse(saved);
    }

    private Usuario buscarAtivoOuErro(UUID id) {
        Usuario entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        if (!entity.isAtivo()) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + id);
        }
        return entity;
    }

    private Usuario buscarOuErro(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }
}
