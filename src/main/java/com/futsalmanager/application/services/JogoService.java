package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.FinalizarJogoRequest;
import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.JogoMapper;
import com.futsalmanager.application.validators.JogoValidator;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.ResultadoJogo;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JogoService {

    private final JogoRepository jogoRepository;
    private final TimeRepository timeRepository;
    private final UsuarioRepository usuarioRepository;
    private final JogoMapper jogoMapper;
    private final JogoValidator validator;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public JogoService(JogoRepository jogoRepository,
                       TimeRepository timeRepository,
                       UsuarioRepository usuarioRepository,
                       JogoMapper jogoMapper,
                       JogoValidator validator,
                       AuthenticatedUserProvider authenticatedUserProvider) {
        this.jogoRepository = jogoRepository;
        this.timeRepository = timeRepository;
        this.usuarioRepository = usuarioRepository;
        this.jogoMapper = jogoMapper;
        this.validator = validator;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public JogoResponse findById(UUID id) {
        return jogoMapper.toResponse(buscarOuErro(id));
    }

    @Transactional(readOnly = true)
    public List<JogoResponse> findAll() {
        return jogoMapper.toResponseList(
                jogoRepository.findByStatusJogoNotOrderByDataHoraAsc(
                        com.futsalmanager.domain.enums.StatusJogo.CANCELADO));
    }

    @Transactional(readOnly = true)
    public List<JogoResponse> findByTime(UUID timeId) {
        authenticatedUserProvider.validarAcessoAoTime(timeId);
        return jogoMapper.toResponseList(
                jogoRepository.findByTimeIdAndStatusJogoNotOrderByDataHoraAsc(
                        timeId,
                        com.futsalmanager.domain.enums.StatusJogo.CANCELADO));
    }

    @Transactional
    public JogoResponse create(JogoCreateRequest request) {

        authenticatedUserProvider.validarAcessoAoTime(request.timeId());

        // 1. valida existência primeiro
        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + request.timeId()));

        // 2. valida regra de negócio
        validator.validarCreate(request);

        Jogo jogo = jogoMapper.toEntity(request);
        jogo.setTime(time);

        try {
            Jogo saved = jogoRepository.save(jogo);

            log.info("Jogo criado: id={}, timeId={}, adversario={}, dataHora={}",
                    saved.getId(),
                    saved.getTime().getId(),
                    saved.getAdversario(),
                    saved.getDataHora()
            );

            return jogoMapper.toResponse(saved);

        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Conflito de agenda: já existe jogo para este horário");
        }
    }

    @Transactional
    public JogoResponse update(UUID id, JogoUpdateRequest request) {

        Jogo entity = buscarOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());

        // valida regra (inclui status + conflito agenda)
        validator.validarUpdate(id, request, entity);

        jogoMapper.updateEntityFromRequest(request, entity);

        try {
            Jogo updated = jogoRepository.save(entity);

            log.info("Jogo atualizado: id={}, timeId={}, adversario={}, dataHora={}, status={}",
                    updated.getId(),
                    updated.getTime().getId(),
                    updated.getAdversario(),
                    updated.getDataHora(),
                    updated.getStatusJogo()
            );

            return jogoMapper.toResponse(updated);

        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Conflito de agenda ao atualizar jogo");
        }
    }

    @Transactional
    public JogoResponse finalizar(UUID id, FinalizarJogoRequest request) {
        Jogo entity = buscarOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());
        validator.validarPodeFinalizar(entity);

        entity.setStatusJogo(StatusJogo.FINALIZADO);
        entity.setGolsTime(request.golsTime());
        entity.setGolsAdversario(request.golsAdversario());

        if (request.golsTime() > request.golsAdversario())      entity.setResultado(ResultadoJogo.VITORIA);
        else if (request.golsTime() < request.golsAdversario()) entity.setResultado(ResultadoJogo.DERROTA);
        else                                                     entity.setResultado(ResultadoJogo.EMPATE);

        Jogo saved = jogoRepository.save(entity);

        if (request.artilheiros() != null && !request.artilheiros().isEmpty()) {
            Map<UUID, Long> golsPor = request.artilheiros().stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            golsPor.forEach((usuarioId, qtd) ->
                    usuarioRepository.findById(usuarioId).ifPresent(u -> {
                        u.setGols(u.getGols() + qtd.intValue());
                        usuarioRepository.save(u);
                    })
            );
        }

        log.info("Jogo finalizado: id={}, placar={}-{}, resultado={}",
                saved.getId(), saved.getGolsTime(), saved.getGolsAdversario(), saved.getResultado());

        return jogoMapper.toResponse(saved);
    }

    @Transactional
    public JogoResponse cancelar(UUID id) {
        Jogo entity = buscarOuErro(id);
        authenticatedUserProvider.validarAcessoAoTime(entity.getTime().getId());

        // 🔥 agora centralizado
        validator.validarPodeCancelar(entity);

        entity.setStatusJogo(StatusJogo.CANCELADO);

        try {
            Jogo saved = jogoRepository.save(entity);

            log.info("Jogo cancelado: id={}, timeId={}, adversario={}",
                    saved.getId(),
                    saved.getTime().getId(),
                    saved.getAdversario()
            );
            return jogoMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            log.error("Erro ao cancelar jogo", ex); // 👈 temporário
                throw new BusinessException("Erro ao cancelar jogo: conflito de dados");
            }
        }

    private Jogo buscarOuErro(UUID id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + id));
    }
}