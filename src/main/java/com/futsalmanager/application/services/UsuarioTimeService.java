package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.AddMembroRequest;
import com.futsalmanager.api.dto.request.AlterarPerfilMembroRequest;
import com.futsalmanager.api.dto.response.MembroResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Gestão do vínculo (membership) entre um Usuario (identidade) e um Time,
 * incluindo o perfil (ADMIN/ATLETA) que passou a ser por-vínculo em vez de global.
 */
@Service
@Slf4j
public class UsuarioTimeService {

    private final UsuarioTimeRepository usuarioTimeRepository;
    private final UsuarioRepository usuarioRepository;
    private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UsuarioTimeService(UsuarioTimeRepository usuarioTimeRepository, UsuarioRepository usuarioRepository,
                              TimeRepository timeRepository, PasswordEncoder passwordEncoder,
                              AuthenticatedUserProvider authenticatedUserProvider) {
        this.usuarioTimeRepository = usuarioTimeRepository;
        this.usuarioRepository = usuarioRepository;
        this.timeRepository = timeRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    /** Traz ativos e inativos — a tela decide o que mostrar em cada aba (e precisa dos inativos pra poder reativar). */
    @Transactional(readOnly = true)
    public List<MembroResponse> findByTimeId(UUID timeId) {
        authenticatedUserProvider.validarMembro(timeId);
        return usuarioTimeRepository.findByTimeId(timeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MembroResponse addMembro(UUID timeId, AddMembroRequest request) {
        authenticatedUserProvider.validarAdminDoTime(timeId);

        Time time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + timeId));

        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            if (request.nome() == null || request.nome().isBlank()) {
                throw new BusinessException("Nome é obrigatório para cadastrar uma nova pessoa.");
            }
            if (request.senha() == null || request.senha().length() < 6) {
                throw new BusinessException("Senha (mínimo 6 caracteres) é obrigatória para cadastrar uma nova pessoa.");
            }

            Usuario novo = new Usuario();
            novo.setNome(request.nome().trim());
            novo.setEmail(email);
            novo.setSenha(passwordEncoder.encode(request.senha()));
            novo.setAtivo(true);
            novo.setGols(0);

            Usuario salvo = usuarioRepository.save(novo);
            log.info("Identidade criada ao adicionar membro: id={}, email={}", salvo.getId(), salvo.getEmail());
            return salvo;
        });

        PerfilUsuario perfil = request.perfil() != null ? request.perfil() : PerfilUsuario.ATLETA;

        UsuarioTime vinculo = usuarioTimeRepository.findByUsuarioIdAndTimeId(usuario.getId(), timeId)
                .orElse(null);

        if (vinculo != null && vinculo.isAtivo()) {
            throw new BusinessException("Essa pessoa já é membro ativo deste time.");
        }

        if (vinculo == null) {
            vinculo = new UsuarioTime();
            vinculo.setUsuario(usuario);
            vinculo.setTime(time);
        }

        vinculo.setPerfil(perfil);
        vinculo.setAtivo(true);

        UsuarioTime salvo = usuarioTimeRepository.save(vinculo);

        log.info("Membro adicionado: timeId={}, usuarioId={}, perfil={}", timeId, usuario.getId(), perfil);

        return toResponse(salvo);
    }

    @Transactional
    public MembroResponse alterarPerfil(UUID timeId, UUID membroId, AlterarPerfilMembroRequest request) {
        authenticatedUserProvider.validarAdminDoTime(timeId);

        UsuarioTime vinculo = buscarVinculoDoTimeOuErro(timeId, membroId);

        if (request.perfil() == PerfilUsuario.ATLETA
                && vinculo.getPerfil() == PerfilUsuario.ADMIN
                && usuarioTimeRepository.countByTimeIdAndPerfilAndAtivoTrue(timeId, PerfilUsuario.ADMIN) <= 1) {
            throw new BusinessException("Não é possível rebaixar o único administrador do time.");
        }

        vinculo.setPerfil(request.perfil());

        UsuarioTime salvo = usuarioTimeRepository.save(vinculo);

        log.info("Perfil de membro alterado: timeId={}, membroId={}, perfil={}", timeId, membroId, request.perfil());

        return toResponse(salvo);
    }

    @Transactional
    public void removerMembro(UUID timeId, UUID membroId) {
        authenticatedUserProvider.validarAdminDoTime(timeId);

        UsuarioTime vinculo = buscarVinculoDoTimeOuErro(timeId, membroId);

        if (vinculo.getPerfil() == PerfilUsuario.ADMIN
                && usuarioTimeRepository.countByTimeIdAndPerfilAndAtivoTrue(timeId, PerfilUsuario.ADMIN) <= 1) {
            throw new BusinessException("Não é possível remover o único administrador do time.");
        }

        vinculo.setAtivo(false);
        usuarioTimeRepository.save(vinculo);

        log.info("Membro removido do time: timeId={}, membroId={}", timeId, membroId);
    }

    @Transactional
    public MembroResponse reativarMembro(UUID timeId, UUID membroId) {
        authenticatedUserProvider.validarAdminDoTime(timeId);

        UsuarioTime vinculo = usuarioTimeRepository.findById(membroId)
                .filter(v -> v.getTime().getId().equals(timeId))
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado: " + membroId));

        if (vinculo.isAtivo()) {
            throw new BusinessException("Membro já está ativo.");
        }

        vinculo.setAtivo(true);
        UsuarioTime salvo = usuarioTimeRepository.save(vinculo);

        log.info("Membro reativado: timeId={}, membroId={}", timeId, membroId);

        return toResponse(salvo);
    }

    private UsuarioTime buscarVinculoDoTimeOuErro(UUID timeId, UUID membroId) {
        UsuarioTime vinculo = usuarioTimeRepository.findById(membroId)
                .filter(v -> v.getTime().getId().equals(timeId))
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado: " + membroId));

        if (!vinculo.isAtivo()) {
            throw new ResourceNotFoundException("Membro não encontrado: " + membroId);
        }
        return vinculo;
    }

    private MembroResponse toResponse(UsuarioTime vinculo) {
        Usuario usuario = vinculo.getUsuario();
        return new MembroResponse(
                vinculo.getId(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                vinculo.getPerfil(),
                vinculo.isAtivo(),
                vinculo.getDataCriacao()
        );
    }
}
