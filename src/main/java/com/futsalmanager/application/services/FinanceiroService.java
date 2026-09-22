package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.response.ResumoFinanceiroResponse;
import com.futsalmanager.application.mappers.DespesaMapper;
import com.futsalmanager.application.mappers.PagamentoMapper;
import com.futsalmanager.domain.enums.StatusDespesa;
import com.futsalmanager.domain.enums.StatusPagamento;
import com.futsalmanager.infrastructure.repositories.DespesaRepository;
import com.futsalmanager.infrastructure.repositories.PagamentoRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Visão consolidada de caixa do time (entradas de pagamentos x saídas de despesas) —
 * restrita a admins do time, igual às demais consultas financeiras agregadas.
 */
@Service
public class FinanceiroService {

    private final PagamentoRepository pagamentoRepository;
    private final DespesaRepository despesaRepository;
    private final PagamentoMapper pagamentoMapper;
    private final DespesaMapper despesaMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public FinanceiroService(PagamentoRepository pagamentoRepository, DespesaRepository despesaRepository,
                              PagamentoMapper pagamentoMapper, DespesaMapper despesaMapper,
                              AuthenticatedUserProvider authenticatedUserProvider) {
        this.pagamentoRepository = pagamentoRepository;
        this.despesaRepository = despesaRepository;
        this.pagamentoMapper = pagamentoMapper;
        this.despesaMapper = despesaMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse obterResumo(UUID timeId) {
        authenticatedUserProvider.validarAdminDoTime(timeId);

        BigDecimal totalRecebido = pagamentoRepository.sumValorByTimeIdAndStatus(timeId, StatusPagamento.PAGO);
        long quantidadeRecebimentos = pagamentoRepository.countByTimeIdAndStatusPagamento(timeId, StatusPagamento.PAGO);
        BigDecimal totalDespesasPagas = despesaRepository.sumValorPagoByTimeId(timeId);
        BigDecimal saldo = totalRecebido.subtract(totalDespesasPagas);
        BigDecimal totalPendenteReceber = pagamentoRepository.sumValorByTimeIdAndStatus(timeId, StatusPagamento.PENDENTE);
        BigDecimal totalDespesasEmAberto = despesaRepository.sumValorEmAbertoByTimeId(timeId, StatusDespesa.PAGO);

        return new ResumoFinanceiroResponse(
                timeId,
                totalRecebido,
                quantidadeRecebimentos,
                totalDespesasPagas,
                saldo,
                totalPendenteReceber,
                totalDespesasEmAberto,
                pagamentoMapper.toResponseList(
                        pagamentoRepository.findTop10ByTimeIdAndStatusPagamentoOrderByDataAtualizacaoDesc(
                                timeId, StatusPagamento.PAGO)),
                despesaMapper.toResponseList(
                        despesaRepository.findByTimeIdAndStatusNotOrderByMesReferenciaDesc(timeId, StatusDespesa.PAGO))
        );
    }
}
