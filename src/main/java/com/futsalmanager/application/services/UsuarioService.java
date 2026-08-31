package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.UsuarioMapper;
import com.futsalmanager.application.validators.UsuarioValidator;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Identidade do usuário (login, nome, e-mail, senha). Vínculo com time(s) e
 * perfil (ADMIN/ATLETA) são responsabilidade de {@link UsuarioTimeService}.
 */
@Service
@Slf4j
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioValidator validator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UsuarioService(UsuarioRepository repository, UsuarioMapper usuarioMapper, UsuarioValidator validator,
                          PasswordEncoder passwordEncoder, AuthenticatedUserProvider authenticatedUserProvider) {
        this.repository = repository;
        this.usuarioMapper = usuarioMapper;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(UUID id) {
        return usuarioMapper.toResponse(buscarAtivoOuErro(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioMapper.toResponseList(repository.findByAtivoTrue());
    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioUpdateRequest request) {
        Usuario entity = buscarAtivoOuErro(id);

        Usuario logado = authenticatedUserProvider.getUsuarioAutenticado();
        if (!logado.getId().equals(id)) {
            throw new AccessDeniedException("Você só pode editar o próprio perfil.");
        }

        validator.validarUpdate(entity, request);

        usuarioMapper.updateEntityFromRequest(request, entity);

        if (request.email() != null) {
            entity.setEmail(request.email().trim().toLowerCase());
        }

        if (request.senha() != null && !request.senha().isBlank()) {
            entity.setSenha(passwordEncoder.encode(request.senha()));
        }

        Usuario saved = repository.save(entity);

        log.info("Usuário atualizado com sucesso: id={}, nome={}", saved.getId(), saved.getNome());

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
}
