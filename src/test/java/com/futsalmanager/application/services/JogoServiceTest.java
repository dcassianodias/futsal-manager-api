package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.FinalizarJogoRequest;
import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
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
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.GolRegistroRepository;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private JogoMapper jogoMapper;

    @Mock
    private JogoValidator validator;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GolRegistroRepository golRegistroRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private JogoService jogoService;

    private UUID jogoId;
    private UUID timeId;

    @BeforeEach
    void setUp() {
        jogoId = UUID.randomUUID();
        timeId = UUID.randomUUID();
    }

    @Test
    void findById_DeveRetornarJogo_QuandoJogoExiste() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.findById(jogoId);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(jogoMapper).toResponse(jogo);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoJogoNaoExiste() {
        when(jogoRepository.findById(jogoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.findById(jogoId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Jogo não encontrado: " + jogoId);

        verify(jogoRepository).findById(jogoId);
    }

    @Test
    void findAll_DeveRetornarListaDeJogosNaoCancelados() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        List<Jogo> jogos = List.of(jogo);
        List<JogoResponse> responses = List.of(response);

        when(jogoRepository.findByStatusJogoNotOrderByDataHoraAsc(StatusJogo.CANCELADO))
            .thenReturn(jogos);
        when(jogoMapper.toResponseList(jogos)).thenReturn(responses);

        List<JogoResponse> result = jogoService.findAll();

        assertThat(result).isEqualTo(responses);
        verify(jogoRepository).findByStatusJogoNotOrderByDataHoraAsc(StatusJogo.CANCELADO);
    }

    @Test
    void findByTime_DeveRetornarJogosDoTimeNaoCancelados() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        List<Jogo> jogos = List.of(jogo);
        List<JogoResponse> responses = List.of(response);

        when(jogoRepository.findByTimeIdAndStatusJogoNotOrderByDataHoraAsc(timeId, StatusJogo.CANCELADO))
            .thenReturn(jogos);
        when(jogoMapper.toResponseList(jogos)).thenReturn(responses);

        List<JogoResponse> result = jogoService.findByTime(timeId);

        assertThat(result).isEqualTo(responses);
        verify(jogoRepository).findByTimeIdAndStatusJogoNotOrderByDataHoraAsc(timeId, StatusJogo.CANCELADO);
    }

    @Test
    void create_DeveCriarNovoJogo_QuandoDadosValidos() {
        JogoCreateRequest request = mock(JogoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);

        Jogo novoJogo = mock(Jogo.class);
        Jogo saved = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        Time time = new Time(timeId, "Time Teste", null, true, null, null);

        when(jogoMapper.toEntity(request)).thenReturn(novoJogo);
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(jogoRepository.save(novoJogo)).thenReturn(saved);
        when(saved.getTime()).thenReturn(time);
        when(jogoMapper.toResponse(saved)).thenReturn(response);

        JogoResponse result = jogoService.create(request);

        assertThat(result).isEqualTo(response);
        verify(timeRepository).findById(timeId);
        verify(validator).validarCreate(request);
        verify(novoJogo).setTime(time);
        verify(jogoRepository).save(novoJogo);
    }

    @Test
    void create_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        JogoCreateRequest request = mock(JogoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.create(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoInteractions(validator);
        verifyNoMoreInteractions(jogoRepository);
    }

    @Test
    void update_DeveAtualizarJogo_QuandoDadosValidos() {
        JogoUpdateRequest request = mock(JogoUpdateRequest.class);
        Jogo jogo = mock(Jogo.class);
        Time time = mock(Time.class);
        JogoResponse response = mock(JogoResponse.class);

        when(jogo.getTime()).thenReturn(time);
        when(time.getId()).thenReturn(timeId);
        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.update(jogoId, request);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(validator).validarUpdate(jogoId, request, jogo);
        verify(jogoMapper).updateEntityFromRequest(request, jogo);
        verify(jogoRepository).save(jogo);
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoJogoNaoExiste() {
        JogoUpdateRequest request = mock(JogoUpdateRequest.class);
        when(jogoRepository.findById(jogoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.update(jogoId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Jogo não encontrado: " + jogoId);

        verify(jogoRepository).findById(jogoId);
    }

    @Test
    void finalizar_DeveFinalizarJogo_QuandoJogoAgendado() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        Time time = new Time(timeId, "Time Teste", null, true, null, null);
        FinalizarJogoRequest request = new FinalizarJogoRequest(3, 1, null);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogo.getTime()).thenReturn(time);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.finalizar(jogoId, request);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(validator).validarPodeFinalizar(jogo);
        verify(jogo).setStatusJogo(StatusJogo.FINALIZADO);
        verify(jogo).setGolsTime(3);
        verify(jogo).setGolsAdversario(1);
        verify(jogoRepository).save(jogo);
    }

    @Test
    void finalizar_DevePersistirGolRegistro_QuandoArtilheirosInformados() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        Time time = new Time(timeId, "Time Teste", null, true, null, null);
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = mock(Usuario.class);
        FinalizarJogoRequest request = new FinalizarJogoRequest(3, 1, List.of(usuarioId, usuarioId));

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogo.getTime()).thenReturn(time);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuario.getGols()).thenReturn(0);

        jogoService.finalizar(jogoId, request);

        verify(usuario).setGols(2);
        verify(usuarioRepository).save(usuario);
        verify(golRegistroRepository).save(argThat(g ->
            g.getJogo() == jogo && g.getUsuario() == usuario && g.getQuantidade() == 2));
    }

    @Test
    void finalizar_DeveLancarBusinessException_QuandoJogoNaoAgendado() {
        Jogo jogo = mock(Jogo.class);
        Time time = new Time(timeId, "Time Teste", null, true, null, null);
        FinalizarJogoRequest request = new FinalizarJogoRequest(3, 1, null);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        doThrow(new BusinessException("Só é possível finalizar jogos agendados"))
            .when(validator).validarPodeFinalizar(jogo);

        assertThatThrownBy(() -> jogoService.finalizar(jogoId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Só é possível finalizar jogos agendados");

        verify(jogoRepository).findById(jogoId);
        verify(validator).validarPodeFinalizar(jogo);
        verifyNoMoreInteractions(jogoRepository);
    }

    @Test
    void cancelar_DeveCancelarJogo_QuandoJogoAgendado() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        Time time = new Time(timeId, "Time Teste", null, true, null, null);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogo.getTime()).thenReturn(time);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.cancelar(jogoId);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(validator).validarPodeCancelar(jogo);
        verify(jogo).setStatusJogo(StatusJogo.CANCELADO);
        verify(jogoRepository).save(jogo);
    }

    @Test
    void cancelar_DeveLancarBusinessException_QuandoJogoNaoAgendado() {
        Jogo jogo = mock(Jogo.class);
        Time time = new Time(timeId, "Time Teste", null, true, null, null);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogo.getTime()).thenReturn(time);
        doThrow(new BusinessException("Só é possível cancelar jogos agendados"))
            .when(validator).validarPodeCancelar(jogo);

        assertThatThrownBy(() -> jogoService.cancelar(jogoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Só é possível cancelar jogos agendados");

        verify(jogoRepository).findById(jogoId);
        verify(validator).validarPodeCancelar(jogo);
        verifyNoMoreInteractions(jogoRepository);
    }

    @Test
    void buscarFeedPublico_DeveRetornarFeed_QuandoTimePublico() {
        Time time = new Time(timeId, "Time Teste", null, true, null, null);
        time.setPublico(true);

        Jogo proximo = mock(Jogo.class);
        Jogo finalizado1 = mock(Jogo.class);
        Jogo finalizado2 = mock(Jogo.class);
        JogoResponse proximoResponse = mock(JogoResponse.class);
        JogoResponse finalizado1Response = mock(JogoResponse.class);
        JogoResponse finalizado2Response = mock(JogoResponse.class);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(jogoRepository.findByTimeIdAndStatusJogoOrderByDataHoraAsc(timeId, StatusJogo.AGENDADO))
            .thenReturn(List.of(proximo));
        when(jogoRepository.findByTimeIdAndStatusJogoOrderByDataHoraDesc(timeId, StatusJogo.FINALIZADO))
            .thenReturn(List.of(finalizado1, finalizado2));
        when(jogoMapper.toResponse(proximo)).thenReturn(proximoResponse);
        when(jogoMapper.toResponse(finalizado1)).thenReturn(finalizado1Response);
        when(jogoMapper.toResponse(finalizado2)).thenReturn(finalizado2Response);

        TimePublicoResponse result = jogoService.buscarFeedPublico(timeId);

        assertThat(result.id()).isEqualTo(timeId);
        assertThat(result.nome()).isEqualTo("Time Teste");
        assertThat(result.proximoJogo()).isEqualTo(proximoResponse);
        assertThat(result.ultimosResultados()).containsExactly(finalizado1Response, finalizado2Response);
    }

    @Test
    void buscarFeedPublico_DeveRetornarProximoJogoNulo_QuandoNaoHaJogoAgendado() {
        Time time = new Time(timeId, "Time Teste", null, true, null, null);
        time.setPublico(true);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(jogoRepository.findByTimeIdAndStatusJogoOrderByDataHoraAsc(timeId, StatusJogo.AGENDADO))
            .thenReturn(List.of());
        when(jogoRepository.findByTimeIdAndStatusJogoOrderByDataHoraDesc(timeId, StatusJogo.FINALIZADO))
            .thenReturn(List.of());

        TimePublicoResponse result = jogoService.buscarFeedPublico(timeId);

        assertThat(result.proximoJogo()).isNull();
        assertThat(result.ultimosResultados()).isEmpty();
    }

    @Test
    void buscarFeedPublico_DeveLancarResourceNotFoundException_QuandoTimeNaoPublico() {
        Time time = new Time(timeId, "Time Teste", null, true, null, null);
        time.setPublico(false);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));

        assertThatThrownBy(() -> jogoService.buscarFeedPublico(timeId))
            .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(jogoRepository);
    }

    @Test
    void buscarFeedPublico_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.buscarFeedPublico(timeId))
            .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(jogoRepository);
    }
}
