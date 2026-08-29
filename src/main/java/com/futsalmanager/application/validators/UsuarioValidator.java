package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class UsuarioValidator {

    private final UsuarioRepository repository;

    public UsuarioValidator(UsuarioRepository repository) {
        this.repository = repository;
    }

    public void validarCreate(UsuarioCreateRequest request) {

        String email = request.email().toLowerCase();

        if (repository.existsByTimeIdAndEmailAndAtivoTrue(
                request.timeId(),
                email
        )) {
            throw new BusinessException("Já existe um usuário ativo com esse email no time.");
        }
    }

    public void validarUpdate(Usuario entity, UsuarioUpdateRequest request) {
        validarCamposObrigatoriosUpdate(request);
        validarEmailDuplicadoUpdate(entity, request);
    }

    private void validarEmailDuplicadoUpdate(Usuario entity, UsuarioUpdateRequest request) {

        // email é opcional no update → só valida se vier
        if (request.email() == null || request.email().isBlank()) return;

        String email = request.email().toLowerCase();

        if (!entity.getEmail().equals(email)) {
            boolean exists = repository.existsByTimeIdAndEmailAndIdNotAndAtivoTrue(
                    entity.getTime().getId(),
                    email,
                    entity.getId()
            );

            if (exists) {
                throw new BusinessException("Já existe outro usuário com esse email no time.");
            }
        }
    }

    private void validarCamposObrigatoriosUpdate(UsuarioUpdateRequest request) {
        // senha é opcional no update → só valida se vier
        if (request.senha() != null &&
                !request.senha().isBlank()
                && request.senha().length() < 6) {
            throw new BusinessException("Senha deve ter no mínimo 6 caracteres.");
        }
    }
}
