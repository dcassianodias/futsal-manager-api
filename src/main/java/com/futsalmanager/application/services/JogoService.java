package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.FinalizarJogoRequest;
import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.ArtilheiroResponse;
import com.futsalmanager.api.dto.response.FeedArtilheiroResponse;
import com.futsalmanager.api.dto.response.FeedJogoResponse;
import com.futsalmanager.api.dto.response.FeedPublicoResponse;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.api.dto.response.TimePublicoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.JogoMapper;
import com.futsalmanager.application.validators.JogoValidator;
import com.futsalmanager.domain.entities.GolRegistro;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.QuadroTime;
import com.futsalmanager.domain.enums.ResultadoJogo;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.AproveitamentoProjection;
import com.futsalmanager.infrastructure.repositories.ArtilheiroProjection;
import com.futsalmanager.infrastructure.repositories.GolRegistroRepository;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
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
    private final GolRegistroRepository golRegistroRepository;
    private final JogoMapper jogoMapper;
    private final JogoValidator validator;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public JogoService(JogoRepository jogoRepository,
                       TimeRepository timeRepository,
                       UsuarioRepository usuarioRepository,
                       GolRegistroRepository golRegistroRepository,
                       JogoMapper jogoMapper,
                       JogoValidator validator,
                       AuthenticatedUserProvider authenticatedUserProvider) {
        this.jogoRepository = jogoRepository;
        this.timeRepository = timeRepository;
        this.usuarioRepository = usuarioRepository;
        this.golRegistroRepository = golRegistroRepository;
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
        authenticatedUserProvider.validarMembro(timeId);
        return jogoMapper.toResponseList(
                jogoRepository.findByTimeIdAndStatusJogoNotOrderByDataHoraAsc(
                        timeId,
                        com.futsalmanager.domain.enums.StatusJogo.CANCELADO));
    }

    @Transactional
    public JogoResponse create(JogoCreateRequest request) {

        authenticatedUserProvider.validarAdminDoTime(request.timeId());

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
        authenticatedUserProvider.validarAdminDoTime(entity.getTime().getId());

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
        authenticatedUserProvider.validarAdminDoTime(entity.getTime().getId());
        validator.validarPodeFinalizar(entity);
        validator.validarArtilheiros(request);

        int golsTime = request.golsTimeQuadro1() + request.golsTimeQuadro2();
        int golsAdversario = request.golsAdversarioQuadro1() + request.golsAdversarioQuadro2();

        entity.setStatusJogo(StatusJogo.FINALIZADO);
        entity.setGolsTime(golsTime);
        entity.setGolsAdversario(golsAdversario);
        entity.setGolsTimeQuadro1(request.golsTimeQuadro1());
        entity.setGolsAdversarioQuadro1(request.golsAdversarioQuadro1());
        entity.setGolsTimeQuadro2(request.golsTimeQuadro2());
        entity.setGolsAdversarioQuadro2(request.golsAdversarioQuadro2());

        if (golsTime > golsAdversario)      entity.setResultado(ResultadoJogo.VITORIA);
        else if (golsTime < golsAdversario) entity.setResultado(ResultadoJogo.DERROTA);
        else                                 entity.setResultado(ResultadoJogo.EMPATE);

        Jogo saved = jogoRepository.save(entity);

        registrarArtilheiros(saved, request.artilheirosQuadro1(), QuadroTime.PRIMEIRO);
        registrarArtilheiros(saved, request.artilheirosQuadro2(), QuadroTime.SEGUNDO);

        log.info("Jogo finalizado: id={}, placar={}-{}, resultado={}",
                saved.getId(), saved.getGolsTime(), saved.getGolsAdversario(), saved.getResultado());

        return jogoMapper.toResponse(saved);
    }

    private void registrarArtilheiros(Jogo jogo, List<UUID> artilheiros, QuadroTime quadro) {
        if (artilheiros == null || artilheiros.isEmpty()) return;

        Map<UUID, Long> golsPor = artilheiros.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        golsPor.forEach((usuarioId, qtd) ->
                usuarioRepository.findById(usuarioId).ifPresent(u -> {
                    u.setGols(u.getGols() + qtd.intValue());
                    usuarioRepository.save(u);
                    golRegistroRepository.save(new GolRegistro(jogo, u, qtd.intValue(), quadro));
                })
        );
    }

    @Transactional
    public JogoResponse cancelar(UUID id) {
        Jogo entity = buscarOuErro(id);
        authenticatedUserProvider.validarAdminDoTime(entity.getTime().getId());

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

    @Transactional(readOnly = true)
    public List<ArtilheiroResponse> artilheirosPorTime(UUID timeId, QuadroTime quadro) {
        authenticatedUserProvider.validarMembro(timeId);
        List<ArtilheiroProjection> ranking = quadro == null
                ? golRegistroRepository.rankingPorTime(timeId)
                : golRegistroRepository.rankingPorTimeEQuadro(timeId, quadro);
        return ranking.stream()
                .map(p -> new ArtilheiroResponse(p.getUsuarioId(), p.getNome(), p.getGols()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TimePublicoResponse buscarFeedPublico(UUID timeId) {
        Time time = timeRepository.findById(timeId)
                .filter(Time::getPublico)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));

        Jogo proximo = jogoRepository.findByTimeIdAndStatusJogoOrderByDataHoraAsc(timeId, StatusJogo.AGENDADO)
                .stream().findFirst().orElse(null);

        List<JogoResponse> ultimos = jogoRepository.findByTimeIdAndStatusJogoOrderByDataHoraDesc(timeId, StatusJogo.FINALIZADO)
                .stream().limit(5).map(jogoMapper::toResponse).toList();

        return new TimePublicoResponse(
                time.getId(),
                time.getNome(),
                proximo != null ? jogoMapper.toResponse(proximo) : null,
                ultimos
        );
    }

    @Transactional(readOnly = true)
    public FeedPublicoResponse buscarFeedAgregado() {
        List<Jogo> finalizados = jogoRepository.findTop12ByTimePublicoTrueAndStatusJogoOrderByDataHoraDesc(StatusJogo.FINALIZADO);
        List<Jogo> agendados = jogoRepository.findTop20ByTimePublicoTrueAndStatusJogoOrderByDataHoraAsc(StatusJogo.AGENDADO);

        List<UUID> idsFinalizados = finalizados.stream().map(Jogo::getId).toList();
        Map<UUID, String> destaquePorJogo = golRegistroRepository.findByJogoIdIn(idsFinalizados).stream()
                .collect(Collectors.groupingBy(g -> g.getJogo().getId()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    GolRegistro top = e.getValue().stream()
                            .max(Comparator.comparingInt(GolRegistro::getQuantidade))
                            .orElseThrow();
                    return top.getUsuario().getNome() + " — " + top.getQuantidade()
                            + (top.getQuantidade() == 1 ? " gol" : " gols");
                }));

        List<Jogo> todos = new ArrayList<>(finalizados);
        todos.addAll(agendados);

        List<UUID> idsTimes = todos.stream().map(j -> j.getTime().getId()).distinct().toList();
        Map<UUID, Integer> aproveitamentoPorTime = jogoRepository
                .aproveitamentoPorTimes(idsTimes, StatusJogo.FINALIZADO, ResultadoJogo.VITORIA).stream()
                .collect(Collectors.toMap(AproveitamentoProjection::getTimeId,
                        p -> p.getTotal() > 0 ? Math.round(p.getVitorias() * 100f / p.getTotal()) : 0));

        List<FeedJogoResponse> jogos = todos.stream()
                .map(j -> new FeedJogoResponse(
                        j.getTime().getId(),
                        j.getTime().getNome(),
                        j.getAdversario(),
                        j.getLocal(),
                        j.getStatusJogo(),
                        j.getDataHora(),
                        j.getGolsTime(),
                        j.getGolsAdversario(),
                        destaquePorJogo.get(j.getId()),
                        aproveitamentoPorTime.get(j.getTime().getId())
                ))
                .toList();

        List<FeedArtilheiroResponse> artilheiros = golRegistroRepository.rankingPublico().stream()
                .limit(5)
                .map(p -> new FeedArtilheiroResponse(p.getUsuarioId(), p.getNome(), p.getTimeNome(), p.getGols()))
                .toList();

        return new FeedPublicoResponse(jogos, artilheiros);
    }

    private Jogo buscarOuErro(UUID id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + id));
    }
}