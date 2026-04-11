package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.GerarMensalidadeRequest;
import com.futsalmanager.api.dto.request.PagamentoCreateRequest;
import com.futsalmanager.api.dto.request.PagamentoUpdateRequest;
import com.futsalmanager.api.dto.response.GerarMensalidadeResponse;
import com.futsalmanager.api.dto.response.PagamentoResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.PagamentoMapper;
import com.futsalmanager.application.validators.PagamentoValidator;
import com.futsalmanager.domain.entities.Pagamento;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.domain.enums.StatusPagamento;
import com.futsalmanager.domain.enums.TipoPagamento;
import com.futsalmanager.infrastructure.repositories.EventoRepository;
import com.futsalmanager.infrastructure.repositories.PagamentoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
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
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private PagamentoMapper pagamentoMapper;

    @Mock
    private PagamentoValidator validator;

    @InjectMocks
    private PagamentoService pagamentoService;

    private UUID pagamentoId;
    private UUID timeId;
    private UUID usuarioId;
    private UUID eventoId;

    @BeforeEach
    void setUp() {
        pagamentoId = UUID.randomUUID();
        timeId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        eventoId = UUID.randomUUID();
    }

    @Test
    void findById_DeveRetornarPagamento_QuandoPagamentoExiste() {
        Pagamento pagamento = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoMapper.toResponse(pagamento)).thenReturn(response);

        PagamentoResponse result = pagamentoService.findById(pagamentoId);

        assertThat(result).isEqualTo(response);
        verify(pagamentoRepository).findById(pagamentoId);
        verify(pagamentoMapper).toResponse(pagamento);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoPagamentoNaoExiste() {
        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.findById(pagamentoId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Pagamento não encontrado: " + pagamentoId);

        verify(pagamentoRepository).findById(pagamentoId);
    }

    @Test
    void findAll_DeveRetornarListaDePagamentos() {
        Pagamento pagamento = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);
        List<Pagamento> pagamentos = List.of(pagamento);
        List<PagamentoResponse> responses = List.of(response);

        when(pagamentoRepository.findAll()).thenReturn(pagamentos);
        when(pagamentoMapper.toResponseList(pagamentos)).thenReturn(responses);

        List<PagamentoResponse> result = pagamentoService.findAll();

        assertThat(result).isEqualTo(responses);
        verify(pagamentoRepository).findAll();
        verify(pagamentoMapper).toResponseList(pagamentos);
    }

    @Test
    void findByTime_DeveRetornarPagamentosDoTime() {
        Pagamento pagamento = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);
        List<Pagamento> pagamentos = List.of(pagamento);
        List<PagamentoResponse> responses = List.of(response);

        when(pagamentoRepository.findByTimeIdOrderByDataCriacaoDesc(timeId)).thenReturn(pagamentos);
        when(pagamentoMapper.toResponseList(pagamentos)).thenReturn(responses);

        List<PagamentoResponse> result = pagamentoService.findByTime(timeId);

        assertThat(result).isEqualTo(responses);
        verify(pagamentoRepository).findByTimeIdOrderByDataCriacaoDesc(timeId);
    }

    @Test
    void create_DeveCriarNovoPagamento_QuandoDadosValidos() {
        PagamentoCreateRequest request = mock(PagamentoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(request.usuarioId()).thenReturn(usuarioId);
        when(request.tipo()).thenReturn(TipoPagamento.MENSALIDADE);

        Time time = mock(Time.class);
        Usuario usuario = mock(Usuario.class);
        Pagamento novoPagamento = mock(Pagamento.class);
        Pagamento saved = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(pagamentoMapper.toEntity(request)).thenReturn(novoPagamento);
        when(pagamentoRepository.save(novoPagamento)).thenReturn(saved);
        when(pagamentoMapper.toResponse(saved)).thenReturn(response);

        PagamentoResponse result = pagamentoService.create(request);

        assertThat(result).isEqualTo(response);
        verify(timeRepository).findById(timeId);
        verify(usuarioRepository).findById(usuarioId);
        verify(validator).validarCreate(any(), any(), any());
        verify(pagamentoRepository).save(novoPagamento);
    }

    @Test
    void create_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        PagamentoCreateRequest request = mock(PagamentoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.create(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(usuarioRepository, eventoRepository, pagamentoRepository);
    }

    @Test
    void update_DeveAtualizarPagamento_QuandoDadosValidos() {
        PagamentoUpdateRequest request = mock(PagamentoUpdateRequest.class);
        Pagamento pagamento = mock(Pagamento.class);
        when(pagamento.getTipoPagamento()).thenReturn(TipoPagamento.MENSALIDADE);
        when(pagamento.getStatusPagamento()).thenReturn(StatusPagamento.PENDENTE);
        PagamentoResponse response = mock(PagamentoResponse.class);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);
        when(pagamentoMapper.toResponse(pagamento)).thenReturn(response);

        PagamentoResponse result = pagamentoService.update(pagamentoId, request);

        assertThat(result).isEqualTo(response);
        verify(pagamentoRepository).findById(pagamentoId);
        verify(validator).validarUpdate(any(), any());
        verify(pagamentoMapper).updateEntityFromRequest(request, pagamento);
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoPagamentoNaoExiste() {
        PagamentoUpdateRequest request = mock(PagamentoUpdateRequest.class);
        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.update(pagamentoId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Pagamento não encontrado: " + pagamentoId);

        verify(pagamentoRepository).findById(pagamentoId);
    }

    @Test
    void gerarMensalidades_DeveGerarMensalidadesParaTodosAtletas() {
        GerarMensalidadeRequest request = mock(GerarMensalidadeRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(request.mesReferencia()).thenReturn(java.time.LocalDate.of(2026, 4, 1));

        Time time = mock(Time.class);
        when(time.getValorMensalidade()).thenReturn(java.math.BigDecimal.valueOf(50.00));
        when(time.getId()).thenReturn(timeId);

        Usuario atleta = mock(Usuario.class);
        List<Usuario> atletas = List.of(atleta);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioRepository.findByTimeIdAndPerfilAndAtivoTrue(timeId, PerfilUsuario.ATLETA))
            .thenReturn(atletas);
        when(pagamentoRepository.existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamento(
            any(), any(), any(), any()
        )).thenReturn(false);

        GerarMensalidadeResponse result = pagamentoService.gerarMensalidades(request);

        assertThat(result.timeId()).isEqualTo(timeId);
        assertThat(result.mesReferencia()).isEqualTo(java.time.LocalDate.of(2026, 4, 1));
        verify(timeRepository).findById(timeId);
        verify(usuarioRepository).findByTimeIdAndPerfilAndAtivoTrue(timeId, PerfilUsuario.ATLETA);
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    void gerarMensalidades_DeveLancarBusinessException_QuandoTimeNaoPossuiMensalidade() {
        GerarMensalidadeRequest request = mock(GerarMensalidadeRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(request.mesReferencia()).thenReturn(java.time.LocalDate.of(2026, 4, 1));

        Time time = mock(Time.class);
        when(time.getValorMensalidade()).thenReturn(null);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));

        assertThatThrownBy(() -> pagamentoService.gerarMensalidades(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Time não possui mensalidade configurada");

        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(usuarioRepository, pagamentoRepository);
    }

    @Test
    void cancelar_DeveCancelarPagamento_QuandoPagamentoPendente() {
        Pagamento pagamento = mock(Pagamento.class);
        when(pagamento.getStatusPagamento()).thenReturn(StatusPagamento.PENDENTE);
        PagamentoResponse response = mock(PagamentoResponse.class);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);
        when(pagamentoMapper.toResponse(pagamento)).thenReturn(response);

        PagamentoResponse result = pagamentoService.cancelar(pagamentoId);

        assertThat(result).isEqualTo(response);
        verify(pagamentoRepository).findById(pagamentoId);
        verify(pagamento).setStatusPagamento(StatusPagamento.CANCELADO);
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void cancelar_DeveLancarBusinessException_QuandoPagamentoPago() {
        Pagamento pagamento = mock(Pagamento.class);
        when(pagamento.getStatusPagamento()).thenReturn(StatusPagamento.PAGO);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        assertThatThrownBy(() -> pagamentoService.cancelar(pagamentoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Não é possível cancelar um pagamento já pago.");

        verify(pagamentoRepository).findById(pagamentoId);
        verifyNoMoreInteractions(pagamentoRepository);
    }

    @Test
    void findPendentesByUsuario_DeveRetornarPagamentosPendentesDoUsuario() {
        Pagamento pagamento = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);
        List<Pagamento> pagamentos = List.of(pagamento);
        List<PagamentoResponse> responses = List.of(response);

        when(pagamentoRepository.findByUsuarioIdAndStatusPagamentoOrderByDataCriacaoDesc(usuarioId, StatusPagamento.PENDENTE))
            .thenReturn(pagamentos);
        when(pagamentoMapper.toResponseList(pagamentos)).thenReturn(responses);

        List<PagamentoResponse> result = pagamentoService.findPendentesByUsuario(usuarioId);

        assertThat(result).isEqualTo(responses);
        verify(pagamentoRepository).findByUsuarioIdAndStatusPagamentoOrderByDataCriacaoDesc(usuarioId, StatusPagamento.PENDENTE);
    }

    @Test
    void marcarComoPago_DeveMarcarPagamentoComoPago_QuandoPagamentoPendente() {
        Pagamento pagamento = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);
        when(pagamentoMapper.toResponse(pagamento)).thenReturn(response);

        PagamentoResponse result = pagamentoService.marcarComoPago(pagamentoId);

        assertThat(result).isEqualTo(response);
        verify(pagamentoRepository).findById(pagamentoId);
        verify(pagamento).pagar();
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void marcarComoPago_DeveLancarBusinessException_QuandoPagamentoJaPago() {
        Pagamento pagamento = mock(Pagamento.class);
        when(pagamento.getStatusPagamento()).thenReturn(StatusPagamento.PAGO);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        assertThatThrownBy(() -> pagamentoService.marcarComoPago(pagamentoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Pagamento já está marcado como pago");

        verify(pagamentoRepository).findById(pagamentoId);
        verifyNoMoreInteractions(pagamentoRepository);
    }
}





