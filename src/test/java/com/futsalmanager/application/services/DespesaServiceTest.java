package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.DespesaCreateRequest;
import com.futsalmanager.api.dto.request.DespesaUpdateRequest;
import com.futsalmanager.api.dto.response.DespesaResponse;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.DespesaMapper;
import com.futsalmanager.application.validators.DespesaValidator;
import com.futsalmanager.domain.entities.Despesa;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.DespesaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    @Mock
    private DespesaRepository despesaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private DespesaMapper despesaMapper;

    @Mock
    private DespesaValidator validator;

    @InjectMocks
    private DespesaService despesaService;

    private UUID despesaId;
    private UUID timeId;

    @BeforeEach
    void setUp() {
        despesaId = UUID.randomUUID();
        timeId = UUID.randomUUID();
    }

    @Test
    void findById_DeveRetornarDespesa_QuandoDespesaExiste() {
        Despesa despesa = mock(Despesa.class);
        DespesaResponse response = mock(DespesaResponse.class);

        when(despesaRepository.findById(despesaId)).thenReturn(Optional.of(despesa));
        when(despesaMapper.toResponse(despesa)).thenReturn(response);

        DespesaResponse result = despesaService.findById(despesaId);

        assertThat(result).isEqualTo(response);
        verify(despesaRepository).findById(despesaId);
        verify(despesaMapper).toResponse(despesa);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoDespesaNaoExiste() {
        when(despesaRepository.findById(despesaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despesaService.findById(despesaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Despesa não encontrada: " + despesaId);

        verify(despesaRepository).findById(despesaId);
    }

    @Test
    void findAll_DeveRetornarListaDeDespesas() {
        Despesa despesa = mock(Despesa.class);
        DespesaResponse response = mock(DespesaResponse.class);
        List<Despesa> despesas = List.of(despesa);
        List<DespesaResponse> responses = List.of(response);

        when(despesaRepository.findAll()).thenReturn(despesas);
        when(despesaMapper.toResponseList(despesas)).thenReturn(responses);

        List<DespesaResponse> result = despesaService.findAll();

        assertThat(result).isEqualTo(responses);
        verify(despesaRepository).findAll();
        verify(despesaMapper).toResponseList(despesas);
    }

    @Test
    void findByTime_DeveRetornarDespesasDoTime() {
        Despesa despesa = mock(Despesa.class);
        DespesaResponse response = mock(DespesaResponse.class);
        List<Despesa> despesas = List.of(despesa);
        List<DespesaResponse> responses = List.of(response);

        when(despesaRepository.findByTimeIdOrderByMesReferenciaDesc(timeId)).thenReturn(despesas);
        when(despesaMapper.toResponseList(despesas)).thenReturn(responses);

        List<DespesaResponse> result = despesaService.findByTime(timeId);

        assertThat(result).isEqualTo(responses);
        verify(despesaRepository).findByTimeIdOrderByMesReferenciaDesc(timeId);
    }

    @Test
    void create_DeveCriarNovaDespesa_QuandoDadosValidos() {
        DespesaCreateRequest request = mock(DespesaCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);

        Despesa novaDespesa = mock(Despesa.class);
        Despesa saved = mock(Despesa.class);
        DespesaResponse response = mock(DespesaResponse.class);
        Time time = mock(Time.class);

        when(despesaMapper.toEntity(request)).thenReturn(novaDespesa);
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(despesaRepository.save(novaDespesa)).thenReturn(saved);
        when(despesaMapper.toResponse(saved)).thenReturn(response);

        DespesaResponse result = despesaService.create(request);

        assertThat(result).isEqualTo(response);
        verify(validator).validarCreate(request);
        verify(timeRepository).findById(timeId);
        verify(novaDespesa).setTime(time);
        verify(despesaRepository).save(novaDespesa);
    }

    @Test
    void create_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        DespesaCreateRequest request = mock(DespesaCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despesaService.create(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(validator).validarCreate(request);
        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(despesaRepository);
    }

    @Test
    void update_DeveAtualizarDespesa_QuandoDadosValidos() {
        DespesaUpdateRequest request = mock(DespesaUpdateRequest.class);
        Despesa despesa = mock(Despesa.class);
        DespesaResponse response = mock(DespesaResponse.class);

        when(despesaRepository.findById(despesaId)).thenReturn(Optional.of(despesa));
        when(despesaRepository.save(despesa)).thenReturn(despesa);
        when(despesaMapper.toResponse(despesa)).thenReturn(response);

        DespesaResponse result = despesaService.update(despesaId, request);

        assertThat(result).isEqualTo(response);
        verify(despesaRepository).findById(despesaId);
        verify(validator).validarUpdate(request);
        verify(despesaMapper).updateEntityFromRequest(request, despesa);
        verify(despesaRepository).save(despesa);
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoDespesaNaoExiste() {
        DespesaUpdateRequest request = mock(DespesaUpdateRequest.class);
        when(despesaRepository.findById(despesaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despesaService.update(despesaId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Despesa não encontrada: " + despesaId);

        verify(despesaRepository).findById(despesaId);
    }

    @Test
    void delete_DeveDeletarDespesa_QuandoDespesaExiste() {
        Despesa despesa = mock(Despesa.class);
        when(despesaRepository.findById(despesaId)).thenReturn(Optional.of(despesa));

        despesaService.delete(despesaId);

        verify(despesaRepository).findById(despesaId);
        verify(despesaRepository).delete(despesa);
    }

    @Test
    void delete_DeveLancarResourceNotFoundException_QuandoDespesaNaoExiste() {
        when(despesaRepository.findById(despesaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despesaService.delete(despesaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Despesa não encontrada: " + despesaId);

        verify(despesaRepository).findById(despesaId);
    }
}
