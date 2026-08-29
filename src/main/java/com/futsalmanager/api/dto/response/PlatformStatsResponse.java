package com.futsalmanager.api.dto.response;

public record PlatformStatsResponse(
        long totalTimes,
        long totalTimesAtivos,
        long totalUsuarios,
        long totalUsuariosAtivos,
        long totalAdmins,
        long totalAtletas,
        long totalJogos,
        long totalJogosAgendados,
        long totalJogosFinalizados,
        long totalPagamentos,
        long totalPagamentosPendentes
) {
}
