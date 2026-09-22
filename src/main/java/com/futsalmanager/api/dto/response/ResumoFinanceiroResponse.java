package com.futsalmanager.api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ResumoFinanceiroResponse(
        UUID timeId,
        BigDecimal totalRecebido,
        long quantidadeRecebimentos,
        BigDecimal totalDespesasPagas,
        BigDecimal saldo,
        BigDecimal totalPendenteReceber,
        BigDecimal totalDespesasEmAberto,
        List<PagamentoResponse> ultimasEntradas,
        List<DespesaResponse> despesasEmAberto
) {
}
