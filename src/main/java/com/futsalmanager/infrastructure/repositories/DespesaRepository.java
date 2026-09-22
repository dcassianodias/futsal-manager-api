package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Despesa;
import com.futsalmanager.domain.enums.StatusDespesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, UUID> {

    List<Despesa> findByTimeIdOrderByMesReferenciaDesc(UUID timeId);

    List<Despesa> findByTimeIdAndStatusNotOrderByMesReferenciaDesc(UUID timeId, StatusDespesa status);

    @Query("SELECT COALESCE(SUM(d.valorPago), 0) FROM Despesa d WHERE d.time.id = :timeId")
    BigDecimal sumValorPagoByTimeId(@Param("timeId") UUID timeId);

    @Query("SELECT COALESCE(SUM(d.valor - d.valorPago), 0) FROM Despesa d WHERE d.time.id = :timeId AND d.status <> :statusExcluido")
    BigDecimal sumValorEmAbertoByTimeId(@Param("timeId") UUID timeId, @Param("statusExcluido") StatusDespesa statusExcluido);

}
