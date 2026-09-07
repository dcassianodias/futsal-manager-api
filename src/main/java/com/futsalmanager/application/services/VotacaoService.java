package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.VotarMelhorRodadaRequest;
import com.futsalmanager.api.dto.response.ResultadoVotacaoResponse;
import com.futsalmanager.api.dto.response.VotoContagemResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.VotoMelhorRodada;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import com.futsalmanager.infrastructure.repositories.VotoMelhorRodadaRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class VotacaoService {

    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioTimeRepository usuarioTimeRepository;
    private final VotoMelhorRodadaRepository votoRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public VotacaoService(JogoRepository jogoRepository,
                           UsuarioRepository usuarioRepository,
                           UsuarioTimeRepository usuarioTimeRepository,
                           VotoMelhorRodadaRepository votoRepository,
                           AuthenticatedUserProvider authenticatedUserProvider) {
        this.jogoRepository = jogoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioTimeRepository = usuarioTimeRepository;
        this.votoRepository = votoRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional
    public ResultadoVotacaoResponse votar(UUID jogoId, VotarMelhorRodadaRequest request) {
        Jogo jogo = buscarOuErro(jogoId);
        UUID timeId = jogo.getTime().getId();
        Usuario votante = authenticatedUserProvider.getUsuarioAutenticado();

        if (jogo.getStatusJogo() != StatusJogo.FINALIZADO) {
            throw new BusinessException("Só é possível votar em jogos finalizados");
        }

        authenticatedUserProvider.validarMembro(timeId);

        if (request.votadoId().equals(votante.getId())) {
            throw new BusinessException("Não é possível votar em si mesmo");
        }

        if (!usuarioTimeRepository.existsByUsuarioIdAndTimeIdAndAtivoTrue(request.votadoId(), timeId)) {
            throw new BusinessException("O usuário votado não pertence a este time");
        }

        if (votoRepository.existsByJogoIdAndVotanteId(jogoId, votante.getId())) {
            throw new BusinessException("Você já votou nesta rodada");
        }

        Usuario votado = usuarioRepository.findById(request.votadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.votadoId()));

        try {
            votoRepository.save(new VotoMelhorRodada(jogo, votante, votado));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Você já votou nesta rodada");
        }

        log.info("Voto melhor da rodada: jogoId={}, votanteId={}, votadoId={}", jogoId, votante.getId(), votado.getId());

        return buscarResultado(jogoId);
    }

    @Transactional(readOnly = true)
    public ResultadoVotacaoResponse buscarResultado(UUID jogoId) {
        Jogo jogo = buscarOuErro(jogoId);
        authenticatedUserProvider.validarMembro(jogo.getTime().getId());

        UUID usuarioId = authenticatedUserProvider.getUsuarioAutenticado().getId();
        UUID meuVoto = votoRepository.findByJogoIdAndVotanteId(jogoId, usuarioId)
                .map(v -> v.getVotado().getId())
                .orElse(null);

        var resultado = votoRepository.contarPorJogo(jogoId).stream()
                .map(p -> new VotoContagemResponse(p.getUsuarioId(), p.getNome(), p.getVotos()))
                .toList();

        return new ResultadoVotacaoResponse(resultado, meuVoto);
    }

    private Jogo buscarOuErro(UUID id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + id));
    }
}
