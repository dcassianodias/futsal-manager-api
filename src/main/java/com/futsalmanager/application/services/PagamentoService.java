package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.GerarMensalidadeRequest;
import com.futsalmanager.api.dto.request.PagamentoCreateRequest;
import com.futsalmanager.api.dto.request.PagamentoUpdateRequest;
import com.futsalmanager.api.dto.response.GerarMensalidadeResponse;
import com.futsalmanager.api.dto.response.PagamentoResponse;
import com.futsalmanager.application.mappers.PagamentoMapper;
import com.futsalmanager.domain.entities.Evento;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final TimeRepository timeRepository;
    private UsuarioRepository usuarioRepository;
    private EventoRepository eventoRepository;
    private final PagamentoMapper pagamentoMapper;

    public PagamentoService(PagamentoRepository pagamentoRepository, TimeRepository timeRepository,
                            UsuarioRepository usuarioRepository, EventoRepository eventoRepository,
                            PagamentoMapper pagamentoMapper) {
        this.pagamentoRepository = pagamentoRepository;
        this.timeRepository = timeRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.pagamentoMapper = pagamentoMapper;
    }

    @Transactional(readOnly = true)
    public PagamentoResponse findById(UUID id) {
        Pagamento entity = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado: " + id));
        return pagamentoMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> findAll() {
        return pagamentoMapper.toResponseList(pagamentoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> findByTime(UUID timeId){
        List<Pagamento> list = pagamentoRepository.findByTimeIdOrderByDataCriacaoDesc(timeId);
        return pagamentoMapper.toResponseList(list);
    }

    @Transactional
    public PagamentoResponse create(PagamentoCreateRequest request) {
        if (request.timeId() == null) {
            throw new IllegalArgumentException("Time do pagamento é obrigatório.");
        }

        if (request.usuarioId() == null) {
            throw new IllegalArgumentException("Usuário do pagamento é obrigatório.");
        }

        if (request.valor() == null || request.valor().signum() <= 0) {
            throw new IllegalArgumentException("Valor do pagamento é obrigatório.");
        }

        if (request.tipo() == null) {
            throw new IllegalArgumentException("Tipo do pagamento é obrigatório.");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + request.usuarioId()));

        if (!usuario.getTime().getId().equals(time.getId())) {
            throw new IllegalArgumentException("Usuário não pertence ao time informado.");
        }

        Evento evento = null;

        if (request.tipo() == TipoPagamento.MENSALIDADE) {
            if (request.mesReferencia() == null) {
                throw new IllegalArgumentException("Mês de referência do pagamento é obrigatório para mensalidade.");
            }
            if (request.eventoId() != null) {
                throw new IllegalArgumentException("Mensalidade não pode ter evento associado.");
            }
        }

        if (request.tipo() == TipoPagamento.EVENTO) {
            if (request.eventoId() == null) {
                throw new IllegalArgumentException("Evento do pagamento é obrigatório para pagamento de evento.");
            }
            if (request.mesReferencia() != null) {
                throw new IllegalArgumentException("Pagamento de evento não pode ter mês de referência.");
            }

            evento = eventoRepository.findById(request.eventoId())
                    .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + request.eventoId()));

            if (!evento.getTime().getId().equals(time.getId())) {
                throw new IllegalArgumentException("Evento não pertence ao time informado.");
            }
        }

        Pagamento pagamento = pagamentoMapper.toEntity(request);
        pagamento.setTime(time);
        pagamento.setUsuario(usuario);
        pagamento.setEvento(evento);
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);

        Pagamento saved = pagamentoRepository.save(pagamento);

        log.info("Pagamento criado com sucesso: id={}, valor={}, tipo={}", saved.getId(), saved.getValor(),
                saved.getTipoPagamento());

        return pagamentoMapper.toResponse(saved);
    }

    @Transactional
    public PagamentoResponse update(UUID id, PagamentoUpdateRequest request) {
        Pagamento entity = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado: " + id));

        if (request.valor() == null || request.valor().signum() <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
        }

        entity.setValor(request.valor());

        Pagamento updated = pagamentoRepository.save(entity);

        log.info("Pagamento atualizado com sucesso: id={}, valor={}, tipo={}", updated.getId(), updated.getValor(),
                updated.getTipoPagamento());

        return pagamentoMapper.toResponse(updated);
    }

    @Transactional
    public GerarMensalidadeResponse gerarMensalidades(GerarMensalidadeRequest request) {

        if (request.timeId() == null) {
            throw new IllegalArgumentException("Time é obrigatório para gerar mensalidades.");
        }

        if (request.mesReferencia() == null) {
            throw new IllegalArgumentException("Mês de referência é obrigatório para gerar mensalidades.");
        }

        Time time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new RuntimeException("Time não encontrado: " + request.timeId()));

        List<Usuario> atletas = usuarioRepository
                .findByTimeIdAndPerfilAndAtivoTrue(request.timeId(), PerfilUsuario.ATLETA);

        int totalGerados = 0;
        int totalJaExistentes = 0;

        for (Usuario atleta : atletas) {

            boolean jaExiste = pagamentoRepository.existsByTimeIdAndUsuarioIdAndMesReferenciaAndTipoPagamento(
                    time.getId(),
                    atleta.getId(),
                    request.mesReferencia(),
                    TipoPagamento.MENSALIDADE
            );

            if (jaExiste) {
                totalJaExistentes++;
                continue;
            }

            Pagamento pagamento = new Pagamento();
            pagamento.setTime(time);
            pagamento.setUsuario(atleta);
            pagamento.setEvento(null);
            pagamento.setMesReferencia(request.mesReferencia());
            pagamento.setValor(time.getValorMensalidade());
            pagamento.setTipoPagamento(TipoPagamento.MENSALIDADE);
            pagamento.setStatusPagamento(StatusPagamento.PENDENTE);

            pagamentoRepository.save(pagamento);
            totalGerados++;
        }

        log.info("Mensalidades geradas para o time {} no mês {}. Gerados: {}, já existentes: {}",
                time.getId(), request.mesReferencia(), totalGerados, totalJaExistentes);

        return new GerarMensalidadeResponse(
                time.getId(),
                request.mesReferencia(),
                atletas.size(),
                totalGerados,
                totalJaExistentes
        );
    }

    @Transactional
    public void delete(UUID id) {
        Pagamento entity = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado: " + id));

        pagamentoRepository.delete(entity);

        log.info("Pagamento deletado com sucesso: id={}, valor={}, tipo={}", entity.getId(), entity.getValor(),
                entity.getTipoPagamento());
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> findPendentesByUsuario(UUID usuarioId) {
        return pagamentoMapper.toResponseList(
                pagamentoRepository.findByUsuarioIdAndStatusPagamentoOrderByDataCriacaoDesc(
                        usuarioId, StatusPagamento.PENDENTE
                )
        );
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> findPendentesByTime(UUID timeId) {
        return pagamentoMapper.toResponseList(
                pagamentoRepository.findByTimeIdAndStatusPagamentoOrderByDataCriacaoDesc(
                        timeId, StatusPagamento.PENDENTE
                )
        );
    }

    public PagamentoResponse marcarComoPago(UUID id) {
        Pagamento entity = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado: " + id));

        if (entity.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalStateException("Atleta já está marcado como pago");
        }

        entity.setStatusPagamento(StatusPagamento.PAGO);

        Pagamento updated = pagamentoRepository.save(entity);

        log.info("Pagamento marcado como pago com sucesso: id={}, valor={}, tipo={}", updated.getId(), updated.getValor(),
                updated.getTipoPagamento());

        return pagamentoMapper.toResponse(updated);

    }

}
