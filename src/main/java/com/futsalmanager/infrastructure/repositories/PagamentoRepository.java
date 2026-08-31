package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Pagamento;
import com.futsalmanager.domain.enums.StatusPagamento;
import com.futsalmanager.domain.enums.TipoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    List<Pagamento> findByTimeIdOrderByDataCriacaoDesc(UUID timeId);

    List<Pagamento> findByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    long countByStatusPagamento(StatusPagamento statusPagamento);

    List<Pagamento> findByUsuarioIdAndStatusPagamentoOrderByDataCriacaoDesc(
            UUID usuarioId, StatusPagamento statusPagamento
    );

    List<Pagamento> findByTimeIdAndStatusPagamentoOrderByDataCriacaoDesc(
            UUID timeId, StatusPagamento statusPagamento
    );

    boolean existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamento(
            UUID timeId,
            UUID usuarioId,
            LocalDate mesReferencia,
            TipoPagamento tipoPagamento
    );

    /** Um pagamento cancelado não deve travar a geração de uma nova mensalidade pro mesmo mês. */
    boolean existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamentoAndStatusPagamentoNot(
            UUID timeId,
            UUID usuarioId,
            LocalDate mesReferencia,
            TipoPagamento tipoPagamento,
            StatusPagamento statusPagamento
    );
}
