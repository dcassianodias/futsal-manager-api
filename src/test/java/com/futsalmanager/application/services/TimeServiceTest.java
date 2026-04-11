package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.TimeMapper;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private TimeMapper timeMapper;

    @InjectMocks
    private TimeService timeService;

    private UUID timeId;
    private Time time;
    private TimeResponse timeResponse;
    private TimeCreateRequest createRequest;
    private TimeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        timeId = UUID.randomUUID();

        time = new Time(timeId, "Time Teste", BigDecimal.valueOf(50.00), true, LocalDateTime.now(), LocalDateTime.now());
        time.setCodigo("TIM-123456789");

        timeResponse = new TimeResponse(
            timeId,
            "Time Teste",
            BigDecimal.valueOf(50.00),
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        createRequest = new TimeCreateRequest(
            "Time Teste",
            BigDecimal.valueOf(50.00)
        );

        updateRequest = new TimeUpdateRequest(
            "Time Atualizado",
            BigDecimal.valueOf(60.00),
            true
        );
    }

    @Test
    void findById_DeveRetornarTime_QuandoTimeExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(timeMapper.toResponse(time)).thenReturn(timeResponse);

        // Act
        TimeResponse result = timeService.findById(timeId);

        // Assert
        assertThat(result).isEqualTo(timeResponse);
        verify(timeRepository).findById(timeId);
        verify(timeMapper).toResponse(time);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeService.findById(timeId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoInteractions(timeMapper);
    }

    @Test
    void findAll_DeveRetornarListaDeTimes() {
        // Arrange
        List<Time> times = List.of(time);
        List<TimeResponse> responses = List.of(timeResponse);

        when(timeRepository.findAll()).thenReturn(times);
        when(timeMapper.toResponseList(times)).thenReturn(responses);

        // Act
        List<TimeResponse> result = timeService.findAll();

        // Assert
        assertThat(result).isEqualTo(responses);
        verify(timeRepository).findAll();
        verify(timeMapper).toResponseList(times);
    }

    @Test
    void create_DeveCriarNovoTime_QuandoDadosValidos() {
        // Arrange
        Time novoTime = new Time(null, "Time Teste", BigDecimal.valueOf(50.00), true, null, null);

        when(timeMapper.toEntity(createRequest)).thenReturn(novoTime);
        when(timeRepository.save(any(Time.class))).thenReturn(time);
        when(timeMapper.toResponse(time)).thenReturn(timeResponse);

        // Act
        TimeResponse result = timeService.create(createRequest);

        // Assert
        assertThat(result).isEqualTo(timeResponse);
        verify(timeMapper).toEntity(createRequest);
        verify(timeRepository).save(any(Time.class));
        verify(timeMapper).toResponse(time);
        assertThat(novoTime.getAtivo()).isTrue();
    }

    @Test
    void update_DeveAtualizarTime_QuandoDadosValidos() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(timeRepository.save(time)).thenReturn(time);
        when(timeMapper.toResponse(time)).thenReturn(timeResponse);

        doNothing().when(timeMapper).updateEntityFromRequest(eq(updateRequest), eq(time));

        // Act
        TimeResponse result = timeService.update(timeId, updateRequest);

        // Assert
        assertThat(result).isEqualTo(timeResponse);
        verify(timeRepository).findById(timeId);
        verify(timeMapper).updateEntityFromRequest(updateRequest, time);
        verify(timeRepository).save(time);
        verify(timeMapper).toResponse(time);
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeService.update(timeId, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoInteractions(timeMapper);
    }

    @Test
    void delete_DeveDeletarTime_QuandoTimeExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(timeMapper.toResponse(time)).thenReturn(timeResponse);

        // Act
        timeService.delete(timeId);

        // Assert
        verify(timeRepository).findById(timeId);
        verify(timeMapper).toResponse(time);
        verify(timeRepository).delete(time);
    }

    @Test
    void delete_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeService.delete(timeId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado" + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoInteractions(timeMapper);
        verifyNoMoreInteractions(timeRepository);
    }

    @Test
    void desativar_DeveDesativarTime_QuandoTimeAtivo() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(timeRepository.save(time)).thenReturn(time);

        // Act
        timeService.desativar(timeId);

        // Assert
        verify(timeRepository).findById(timeId);
        verify(timeRepository).save(time);
        assertThat(time.getAtivo()).isFalse();
    }

    @Test
    void desativar_DeveLancarBusinessException_QuandoTimeJaInativo() {
        // Arrange
        time.setAtivo(false);
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));

        // Act & Assert
        assertThatThrownBy(() -> timeService.desativar(timeId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Time já está inativo");

        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(timeRepository);
    }

    @Test
    void desativar_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeService.desativar(timeId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(timeRepository);
    }

    @Test
    void ativar_DeveAtivarTime_QuandoTimeInativo() {
        // Arrange
        time.setAtivo(false);
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(timeRepository.save(time)).thenReturn(time);

        // Act
        timeService.ativar(timeId);

        // Assert
        verify(timeRepository).findById(timeId);
        verify(timeRepository).save(time);
        assertThat(time.getAtivo()).isTrue();
    }

    @Test
    void ativar_DeveLancarBusinessException_QuandoTimeJaAtivo() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));

        // Act & Assert
        assertThatThrownBy(() -> timeService.ativar(timeId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Time já está ativo");

        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(timeRepository);
    }

    @Test
    void ativar_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeService.ativar(timeId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(timeRepository);
    }
}
