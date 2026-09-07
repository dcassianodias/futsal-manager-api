package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.VotarMelhorRodadaRequest;
import com.futsalmanager.api.dto.response.ResultadoVotacaoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import com.futsalmanager.infrastructure.repositories.VotoContagemProjection;
import com.futsalmanager.infrastructure.repositories.VotoMelhorRodadaRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotacaoServiceTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioTimeRepository usuarioTimeRepository;

    @Mock
    private VotoMelhorRodadaRepository votoRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private VotacaoService votacaoService;

    private UUID jogoId;
    private UUID timeId;
    private UUID votanteId;
    private UUID votadoId;
    private Jogo jogo;
    private Time time;
    private Usuario votante;

    @BeforeEach
    void setUp() {
        jogoId = UUID.randomUUID();
        timeId = UUID.randomUUID();
        votanteId = UUID.randomUUID();
        votadoId = UUID.randomUUID();
        time = new Time(timeId, "Time Teste", null, true, null, null);
        jogo = mock(Jogo.class);
        votante = mock(Usuario.class);
    }

    @Test
    void votar_DeveRegistrarVoto_QuandoDadosValidos() {
        Usuario votado = mock(Usuario.class);
        VotarMelhorRodadaRequest request = new VotarMelhorRodadaRequest(votadoId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.FINALIZADO);
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(votante);
        when(votante.getId()).thenReturn(votanteId);
        when(usuarioTimeRepository.existsByUsuarioIdAndTimeIdAndAtivoTrue(votadoId, timeId)).thenReturn(true);
        when(votoRepository.existsByJogoIdAndVotanteId(jogoId, votanteId)).thenReturn(false);
        when(usuarioRepository.findById(votadoId)).thenReturn(Optional.of(votado));
        when(votoRepository.findByJogoIdAndVotanteId(jogoId, votanteId)).thenReturn(Optional.empty());
        when(votoRepository.contarPorJogo(jogoId)).thenReturn(List.of());

        ResultadoVotacaoResponse response = votacaoService.votar(jogoId, request);

        verify(authenticatedUserProvider, org.mockito.Mockito.times(2)).validarMembro(timeId);
        verify(votoRepository).save(argThatVoto(jogo, votante, votado));
        assertThat(response.resultado()).isEmpty();
    }

    @Test
    void votar_DeveLancarBusinessException_QuandoJogoNaoFinalizado() {
        VotarMelhorRodadaRequest request = new VotarMelhorRodadaRequest(votadoId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.AGENDADO);

        assertThatThrownBy(() -> votacaoService.votar(jogoId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Só é possível votar em jogos finalizados");
    }

    @Test
    void votar_DeveLancarBusinessException_QuandoVotarEmSiMesmo() {
        VotarMelhorRodadaRequest request = new VotarMelhorRodadaRequest(votanteId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.FINALIZADO);
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(votante);
        when(votante.getId()).thenReturn(votanteId);

        assertThatThrownBy(() -> votacaoService.votar(jogoId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Não é possível votar em si mesmo");
    }

    @Test
    void votar_DeveLancarBusinessException_QuandoVotadoNaoEhMembroDoTime() {
        VotarMelhorRodadaRequest request = new VotarMelhorRodadaRequest(votadoId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.FINALIZADO);
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(votante);
        when(votante.getId()).thenReturn(votanteId);
        when(usuarioTimeRepository.existsByUsuarioIdAndTimeIdAndAtivoTrue(votadoId, timeId)).thenReturn(false);

        assertThatThrownBy(() -> votacaoService.votar(jogoId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("O usuário votado não pertence a este time");
    }

    @Test
    void votar_DeveLancarBusinessException_QuandoJaVotouNestaRodada() {
        VotarMelhorRodadaRequest request = new VotarMelhorRodadaRequest(votadoId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.FINALIZADO);
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(votante);
        when(votante.getId()).thenReturn(votanteId);
        when(usuarioTimeRepository.existsByUsuarioIdAndTimeIdAndAtivoTrue(votadoId, timeId)).thenReturn(true);
        when(votoRepository.existsByJogoIdAndVotanteId(jogoId, votanteId)).thenReturn(true);

        assertThatThrownBy(() -> votacaoService.votar(jogoId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Você já votou nesta rodada");
    }

    @Test
    void votar_DeveLancarResourceNotFoundException_QuandoJogoNaoExiste() {
        VotarMelhorRodadaRequest request = new VotarMelhorRodadaRequest(votadoId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votacaoService.votar(jogoId, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private com.futsalmanager.domain.entities.VotoMelhorRodada argThatVoto(Jogo jogo, Usuario votante, Usuario votado) {
        return org.mockito.ArgumentMatchers.argThat(v ->
            v.getJogo() == jogo && v.getVotante() == votante && v.getVotado() == votado);
    }
}
