package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.EventoCreateRequest;
import com.futsalmanager.api.dto.request.EventoUpdateRequest;
import com.futsalmanager.api.dto.response.EventoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.EventoMapper;
import com.futsalmanager.application.validators.EventoValidator;
import com.futsalmanager.domain.entities.Evento;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.EventoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private EventoMapper eventoMapper;

    @Mock
    private EventoValidator validator;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private EventoService eventoService;

    private UUID eventoId;
    private UUID timeId;

    @BeforeEach
    void setUp() {
        eventoId = UUID.randomUUID();
        timeId = UUID.randomUUID();
    }

    @Test
    void findById_DeveRetornarEvento_QuandoEventoExiste() {
        Evento evento = mock(Evento.class);
        EventoResponse response = mock(EventoResponse.class);

        when(evento.getTime()).thenReturn(mock(Time.class));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventoMapper.toResponse(evento)).thenReturn(response);

        EventoResponse result = eventoService.findById(eventoId);

        assertThat(result).isEqualTo(response);
        verify(eventoRepository).findById(eventoId);
        verify(eventoMapper).toResponse(evento);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoEventoNaoExiste() {
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.findById(eventoId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Evento não encontrado: " + eventoId);

        verify(eventoRepository).findById(eventoId);
    }

    @Test
    void findAll_DeveRetornarListaDeEventosAtivos() {
        Evento evento = mock(Evento.class);
        EventoResponse response = mock(EventoResponse.class);
        List<Evento> eventos = List.of(evento);
        List<EventoResponse> responses = List.of(response);

        when(eventoRepository.findByAtivoTrue()).thenReturn(eventos);
        when(eventoMapper.toResponseList(eventos)).thenReturn(responses);

        List<EventoResponse> result = eventoService.findAll();

        assertThat(result).isEqualTo(responses);
        verify(eventoRepository).findByAtivoTrue();
        verify(eventoMapper).toResponseList(eventos);
    }

    @Test
    void findByTime_DeveRetornarEventosDoTime() {
        Evento evento = mock(Evento.class);
        EventoResponse response = mock(EventoResponse.class);
        List<Evento> eventos = List.of(evento);
        List<EventoResponse> responses = List.of(response);

        when(eventoRepository.findByTimeIdOrderByDataInicioDesc(timeId)).thenReturn(eventos);
        when(eventoMapper.toResponseList(eventos)).thenReturn(responses);

        List<EventoResponse> result = eventoService.findByTime(timeId);

        assertThat(result).isEqualTo(responses);
        verify(eventoRepository).findByTimeIdOrderByDataInicioDesc(timeId);
    }

    @Test
    void create_DeveCriarNovoEvento_QuandoDadosValidos() {
        EventoCreateRequest request = mock(EventoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);

        Evento novoEvento = mock(Evento.class);
        Evento saved = mock(Evento.class);
        EventoResponse response = mock(EventoResponse.class);
        Time time = mock(Time.class);

        when(eventoMapper.toEntity(request)).thenReturn(novoEvento);
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(eventoRepository.save(novoEvento)).thenReturn(saved);
        when(eventoMapper.toResponse(saved)).thenReturn(response);

        EventoResponse result = eventoService.create(request);

        assertThat(result).isEqualTo(response);
        verify(validator).validarCreate(request);
        verify(timeRepository).findById(timeId);
        verify(novoEvento).setTime(time);
        verify(eventoRepository).save(novoEvento);
    }

    @Test
    void create_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        EventoCreateRequest request = mock(EventoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.create(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(validator).validarCreate(request);
        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(eventoRepository);
    }

    @Test
    void update_DeveAtualizarEvento_QuandoDadosValidos() {
        EventoUpdateRequest request = mock(EventoUpdateRequest.class);
        Evento evento = mock(Evento.class);
        EventoResponse response = mock(EventoResponse.class);

        when(evento.getTime()).thenReturn(mock(Time.class));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);
        when(eventoMapper.toResponse(evento)).thenReturn(response);

        EventoResponse result = eventoService.update(eventoId, request);

        assertThat(result).isEqualTo(response);
        verify(eventoRepository).findById(eventoId);
        verify(validator).validarUpdate(request);
        verify(eventoMapper).updateEntityFromRequest(request, evento);
        verify(eventoRepository).save(evento);
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoEventoNaoExiste() {
        EventoUpdateRequest request = mock(EventoUpdateRequest.class);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.update(eventoId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Evento não encontrado: " + eventoId);

        verify(eventoRepository).findById(eventoId);
    }

    @Test
    void desativar_DeveDesativarEvento_QuandoEventoAtivo() {
        Evento evento = mock(Evento.class);
        when(evento.getAtivo()).thenReturn(true);
        when(evento.getTime()).thenReturn(mock(Time.class));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);

        eventoService.desativar(eventoId);

        verify(eventoRepository).findById(eventoId);
        verify(evento).setAtivo(false);
        verify(eventoRepository).save(evento);
    }

    @Test
    void desativar_DeveLancarBusinessException_QuandoEventoJaInativo() {
        Evento evento = mock(Evento.class);
        when(evento.getAtivo()).thenReturn(false);
        when(evento.getTime()).thenReturn(mock(Time.class));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> eventoService.desativar(eventoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Evento já está inativo");

        verify(eventoRepository).findById(eventoId);
        verifyNoMoreInteractions(eventoRepository);
    }

    @Test
    void ativar_DeveAtivarEvento_QuandoEventoInativo() {
        Evento evento = mock(Evento.class);
        when(evento.getAtivo()).thenReturn(false);
        when(evento.getTime()).thenReturn(mock(Time.class));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);

        eventoService.ativar(eventoId);

        verify(eventoRepository).findById(eventoId);
        verify(evento).setAtivo(true);
        verify(eventoRepository).save(evento);
    }

    @Test
    void ativar_DeveLancarBusinessException_QuandoEventoJaAtivo() {
        Evento evento = mock(Evento.class);
        when(evento.getAtivo()).thenReturn(true);
        when(evento.getTime()).thenReturn(mock(Time.class));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> eventoService.ativar(eventoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Evento já está ativo");

        verify(eventoRepository).findById(eventoId);
        verifyNoMoreInteractions(eventoRepository);
    }
}
