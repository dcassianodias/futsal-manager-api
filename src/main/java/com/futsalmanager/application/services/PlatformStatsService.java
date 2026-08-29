package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.response.PlatformStatsResponse;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.domain.enums.StatusPagamento;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.PagamentoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformStatsService {

    private final TimeRepository timeRepository;
    private final UsuarioRepository usuarioRepository;
    private final JogoRepository jogoRepository;
    private final PagamentoRepository pagamentoRepository;

    public PlatformStatsService(TimeRepository timeRepository, UsuarioRepository usuarioRepository,
                                JogoRepository jogoRepository, PagamentoRepository pagamentoRepository) {
        this.timeRepository = timeRepository;
        this.usuarioRepository = usuarioRepository;
        this.jogoRepository = jogoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public PlatformStatsResponse obter() {
        return new PlatformStatsResponse(
                timeRepository.count(),
                timeRepository.countByAtivoTrue(),
                usuarioRepository.count(),
                usuarioRepository.countByAtivoTrue(),
                usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN),
                usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ATLETA),
                jogoRepository.count(),
                jogoRepository.countByStatusJogo(StatusJogo.AGENDADO),
                jogoRepository.countByStatusJogo(StatusJogo.FINALIZADO),
                pagamentoRepository.count(),
                pagamentoRepository.countByStatusPagamento(StatusPagamento.PENDENTE)
        );
    }
}
