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
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.domain.enums.StatusPagamento;
import com.futsalmanager.domain.enums.TipoPagamento;
import com.futsalmanager.infrastructure.repositories.EventoRepository;
import com.futsalmanager.infrastructure.repositories.PagamentoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private UsuarioTimeRepository usuarioTimeRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private PagamentoMapper pagamentoMapper;

    @Mock
    private PagamentoValidator validator;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

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

        when(pagamento.getTime()).thenReturn(mock(Time.class));
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
        // Ver pagamento de todo mundo é coisa de admin, não de qualquer membro — privacidade.
        verify(authenticatedUserProvider).validarAdminDoTime(timeId);
    }

    @Test
    void findPendentesByTime_DeveExigirAdmin_NaoQualquerMembro() {
        when(pagamentoRepository.findByTimeIdAndStatusPagamentoOrderByDataCriacaoDesc(timeId, StatusPagamento.PENDENTE))
            .thenReturn(List.of());
        when(pagamentoMapper.toResponseList(List.of())).thenReturn(List.of());

        pagamentoService.findPendentesByTime(timeId);

        verify(authenticatedUserProvider).validarAdminDoTime(timeId);
        verify(authenticatedUserProvider, never()).validarMembro(any());
    }

    @Test
    void create_DeveCriarNovoPagamento_QuandoDadosValidos() {
        PagamentoCreateRequest request = mock(PagamentoCreateRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(request.usuarioId()).thenReturn(usuarioId);
        when(request.tipoPagamento()).thenReturn(TipoPagamento.MENSALIDADE);

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
        when(pagamento.getTime()).thenReturn(mock(Time.class));
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
        UsuarioTime vinculoAtleta = mock(UsuarioTime.class);
        when(vinculoAtleta.getUsuario()).thenReturn(atleta);
        List<UsuarioTime> vinculos = List.of(vinculoAtleta);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioTimeRepository.findByTimeIdAndAtivoTrue(timeId)).thenReturn(vinculos);
        when(pagamentoRepository.existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamentoAndStatusPagamentoNot(
            any(), any(), any(), any(), eq(StatusPagamento.CANCELADO)
        )).thenReturn(false);

        GerarMensalidadeResponse result = pagamentoService.gerarMensalidades(request);

        assertThat(result.timeId()).isEqualTo(timeId);
        assertThat(result.mesReferencia()).isEqualTo(java.time.LocalDate.of(2026, 4, 1));
        verify(timeRepository).findById(timeId);
        verify(usuarioTimeRepository).findByTimeIdAndAtivoTrue(timeId);
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    void gerarMensalidades_DeveGerarParaAdminTambem_NaoSoAtleta() {
        // Quem administra o time também joga e também paga — admin não fica de fora.
        GerarMensalidadeRequest request = mock(GerarMensalidadeRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(request.mesReferencia()).thenReturn(java.time.LocalDate.of(2026, 4, 1));

        Time time = mock(Time.class);
        when(time.getValorMensalidade()).thenReturn(java.math.BigDecimal.valueOf(50.00));
        when(time.getId()).thenReturn(timeId);

        Usuario admin = mock(Usuario.class);
        Usuario atleta = mock(Usuario.class);
        UsuarioTime vinculoAdmin = mock(UsuarioTime.class);
        UsuarioTime vinculoAtleta = mock(UsuarioTime.class);
        when(vinculoAdmin.getUsuario()).thenReturn(admin);
        when(vinculoAtleta.getUsuario()).thenReturn(atleta);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioTimeRepository.findByTimeIdAndAtivoTrue(timeId)).thenReturn(List.of(vinculoAdmin, vinculoAtleta));
        when(pagamentoRepository.existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamentoAndStatusPagamentoNot(
            any(), any(), any(), any(), eq(StatusPagamento.CANCELADO)
        )).thenReturn(false);

        GerarMensalidadeResponse result = pagamentoService.gerarMensalidades(request);

        assertThat(result.totalGerados()).isEqualTo(2);
        verify(pagamentoRepository, times(2)).save(any(Pagamento.class));
    }

    @Test
    void gerarMensalidades_DeveGerarNovaPendente_QuandoMensalidadeDoMesFoiCancelada() {
        // Uma mensalidade cancelada não pode travar a geração de uma nova pro mesmo mês.
        GerarMensalidadeRequest request = mock(GerarMensalidadeRequest.class);
        when(request.timeId()).thenReturn(timeId);
        when(request.mesReferencia()).thenReturn(java.time.LocalDate.of(2026, 4, 1));

        Time time = mock(Time.class);
        when(time.getValorMensalidade()).thenReturn(java.math.BigDecimal.valueOf(50.00));
        when(time.getId()).thenReturn(timeId);

        Usuario atleta = mock(Usuario.class);
        UsuarioTime vinculoAtleta = mock(UsuarioTime.class);
        when(vinculoAtleta.getUsuario()).thenReturn(atleta);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioTimeRepository.findByTimeIdAndAtivoTrue(timeId)).thenReturn(List.of(vinculoAtleta));
        // Só existe uma mensalidade CANCELADA pra esse mês — não deve contar como "já existe".
        when(pagamentoRepository.existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamentoAndStatusPagamentoNot(
            any(), any(), any(), any(), eq(StatusPagamento.CANCELADO)
        )).thenReturn(false);

        GerarMensalidadeResponse result = pagamentoService.gerarMensalidades(request);

        assertThat(result.totalGerados()).isEqualTo(1);
        assertThat(result.totalJaExistentes()).isEqualTo(0);
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
        when(pagamento.getTime()).thenReturn(mock(Time.class));
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
        when(pagamento.getTime()).thenReturn(mock(Time.class));

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        assertThatThrownBy(() -> pagamentoService.cancelar(pagamentoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Não é possível cancelar um pagamento já pago.");

        verify(pagamentoRepository).findById(pagamentoId);
        verifyNoMoreInteractions(pagamentoRepository);
    }

    @Test
    void findPendentesByUsuario_DeveRetornarPagamentosPendentesDoUsuario_QuandoConsultaOProprioHistorico() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(usuarioId);
        Pagamento pagamento = mock(Pagamento.class);
        PagamentoResponse response = mock(PagamentoResponse.class);
        List<Pagamento> pagamentos = List.of(pagamento);
        List<PagamentoResponse> responses = List.of(response);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(usuario);
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
        when(pagamento.getTime()).thenReturn(mock(Time.class));
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
        when(pagamento.getTime()).thenReturn(mock(Time.class));

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        assertThatThrownBy(() -> pagamentoService.marcarComoPago(pagamentoId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Pagamento já está marcado como pago");

        verify(pagamentoRepository).findById(pagamentoId);
        verifyNoMoreInteractions(pagamentoRepository);
    }
}





