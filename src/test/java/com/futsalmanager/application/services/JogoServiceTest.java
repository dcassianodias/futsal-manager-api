package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.JogoCreateRequest;
import com.futsalmanager.api.dto.request.JogoUpdateRequest;
import com.futsalmanager.api.dto.response.JogoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.JogoMapper;
import com.futsalmanager.application.validators.JogoValidator;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        when(jogoRepository.findByStatusJogoNotOrderByDataHoraDesc(StatusJogo.CANCELADO))
            .thenReturn(jogos);
        when(jogoMapper.toResponseList(jogos)).thenReturn(responses);

        List<JogoResponse> result = jogoService.findAll();

        assertThat(result).isEqualTo(responses);
        verify(jogoRepository).findByStatusJogoNotOrderByDataHoraDesc(StatusJogo.CANCELADO);
    }

    @Test
    void findByTime_DeveRetornarJogosDoTimeNaoCancelados() {
        Jogo jogo = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        List<Jogo> jogos = List.of(jogo);
        List<JogoResponse> responses = List.of(response);

        when(jogoRepository.findByTimeIdAndStatusJogoNotOrderByDataHoraDesc(timeId, StatusJogo.CANCELADO))
            .thenReturn(jogos);
        when(jogoMapper.toResponseList(jogos)).thenReturn(responses);

        List<JogoResponse> result = jogoService.findByTime(timeId);

        assertThat(result).isEqualTo(responses);
        verify(jogoRepository).findByTimeIdAndStatusJogoNotOrderByDataHoraDesc(timeId, StatusJogo.CANCELADO);
    }

    @Test
    void create_DeveCriarNovoJogo_QuandoDadosValidos() {
        JogoCreateRequest request = mock(JogoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);

        Jogo novoJogo = mock(Jogo.class);
        Jogo saved = mock(Jogo.class);
        JogoResponse response = mock(JogoResponse.class);
        Time time = mock(Time.class);

        when(jogoMapper.toEntity(request)).thenReturn(novoJogo);
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(jogoRepository.save(novoJogo)).thenReturn(saved);
        when(jogoMapper.toResponse(saved)).thenReturn(response);

        JogoResponse result = jogoService.create(request);

        assertThat(result).isEqualTo(response);
        verify(validator).validarCreate(request);
        verify(timeRepository).findById(timeId);
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

        verify(validator).validarCreate(request);
        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(jogoRepository);
    }

    @Test
    void update_DeveAtualizarJogo_QuandoDadosValidos() {
        JogoUpdateRequest request = mock(JogoUpdateRequest.class);
        Jogo jogo = mock(Jogo.class);
        Time time = mock(Time.class);
        when(jogo.getTime()).thenReturn(time);
        when(time.getId()).thenReturn(timeId);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.existsByTimeIdAndAdversarioAndLocalAndDataHoraAndIdNotAndStatusJogoNot(
            any(), any(), any(), any(), any(), any()
        )).thenReturn(false);
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        JogoResponse response = mock(JogoResponse.class);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.update(jogoId, request);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(validator).validarUpdate(any(), any(), any());
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
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.AGENDADO);
        JogoResponse response = mock(JogoResponse.class);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.finalizar(jogoId);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(jogo).setStatusJogo(StatusJogo.FINALIZADO);
        verify(jogoRepository).save(jogo);
    }

    @Test
    void finalizar_DeveLancarBusinessException_QuandoJogoNaoAgendado() {
        Jogo jogo = mock(Jogo.class);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.FINALIZADO);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));

        assertThatThrownBy(() -> jogoService.finalizar(jogoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Somente jogos agendados podem ser finalizados");

        verify(jogoRepository).findById(jogoId);
        verifyNoMoreInteractions(jogoRepository);
    }

    @Test
    void cancelar_DeveCancelarJogo_QuandoJogoAgendado() {
        Jogo jogo = mock(Jogo.class);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.AGENDADO);
        JogoResponse response = mock(JogoResponse.class);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogoMapper.toResponse(jogo)).thenReturn(response);

        JogoResponse result = jogoService.cancelar(jogoId);

        assertThat(result).isEqualTo(response);
        verify(jogoRepository).findById(jogoId);
        verify(jogo).setStatusJogo(StatusJogo.CANCELADO);
        verify(jogoRepository).save(jogo);
    }

    @Test
    void cancelar_DeveLancarBusinessException_QuandoJogoNaoAgendado() {
        Jogo jogo = mock(Jogo.class);
        when(jogo.getStatusJogo()).thenReturn(StatusJogo.FINALIZADO);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));

        assertThatThrownBy(() -> jogoService.cancelar(jogoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Somente jogos agendados podem ser cancelados");

        verify(jogoRepository).findById(jogoId);
        verifyNoMoreInteractions(jogoRepository);
    }
}
