package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.TimeMapper;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import com.futsalmanager.security.service.PlatformOwnerGuard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class TimeService {

    private static final Set<String> TIPOS_IMAGEM_PERMITIDOS = Set.of("image/png", "image/jpeg", "image/webp");
    private static final long TAMANHO_MAXIMO_IMAGEM_BYTES = 2 * 1024 * 1024;

    public record Imagem(byte[] dados, String contentType) {
    }

    private final TimeRepository repository;
    private final TimeMapper timeMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final PlatformOwnerGuard platformOwnerGuard;

    public TimeService(TimeRepository repository, TimeMapper timeMapper,
                       AuthenticatedUserProvider authenticatedUserProvider,
                       PlatformOwnerGuard platformOwnerGuard) {
        this.repository = repository;
        this.timeMapper = timeMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.platformOwnerGuard = platformOwnerGuard;
    }

    @Transactional(readOnly = true)
    public TimeResponse findById(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarMembro(id);
        }

        return timeMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<TimeResponse> findAll() {
        List<Time> list = repository.findAll();
        return timeMapper.toResponseList(list);
    }

    @Transactional
    public TimeResponse create(TimeCreateRequest request) {

        Time entity = timeMapper.toEntity(request);
        entity.setCodigo(gerarCodigo(entity.getNome()));

        Time saved = repository.save(entity);

        log.info("Time criado: id={}, nome={}", saved.getId(), saved.getNome());

        return timeMapper.toResponse(saved);
    }

    @Transactional
    public TimeResponse update(UUID id, TimeUpdateRequest request) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        timeMapper.updateEntityFromRequest(request, entity);

        Time saved = repository.save(entity);

        log.info("Time atualizado: id={}, nome={}", saved.getId(), saved.getNome());

        return timeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado" + id));

        // exclusão definitiva é restrita ao dono da plataforma; admins de time usam desativar()

        repository.delete(entity);
        log.info("Time deletado: id={}, nome={}", entity.getId(), entity.getNome());

    }

    @Transactional
    public void desativar(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        if (!entity.getAtivo()) {
            throw new BusinessException("Time já está inativo");
        }

        entity.setAtivo(false);

        repository.save(entity);

        log.info("Time desativado: id={}, nome={}", entity.getId(), entity.getNome());
    }

    @Transactional
    public void ativar(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        if (entity.getAtivo()) {
            throw new BusinessException("Time já está ativo");
        }

        entity.setAtivo(true);

        repository.save(entity);

        log.info("Time reativado: id={}, nome={}", entity.getId(), entity.getNome());
    }

    @Transactional
    public void tornarPublico(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        if (entity.getPublico()) {
            throw new BusinessException("Time já é público");
        }

        entity.setPublico(true);

        repository.save(entity);

        log.info("Time tornado público: id={}, nome={}", entity.getId(), entity.getNome());
    }

    @Transactional
    public void tornarPrivado(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        if (!entity.getPublico()) {
            throw new BusinessException("Time já é privado");
        }

        entity.setPublico(false);

        repository.save(entity);

        log.info("Time tornado privado: id={}, nome={}", entity.getId(), entity.getNome());
    }

    /**
     * Se o link de convite vazar, o admin gera um código novo e o antigo
     * para de funcionar na hora — mesmo modelo de "gerar novo link" de um
     * grupo do WhatsApp. Não afeta quem já entrou pelo link antigo.
     */
    @Transactional
    public TimeResponse regenerarCodigo(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        entity.setCodigo(gerarCodigo(entity.getNome()));

        Time saved = repository.save(entity);

        log.info("Código de convite regenerado: timeId={}", saved.getId());

        return timeMapper.toResponse(saved);
    }

    private String gerarCodigo(String nome) {
        return nome.substring(0, 3).toUpperCase() + "-" + System.currentTimeMillis();
    }

    @Transactional
    public void atualizarLogo(UUID id, MultipartFile arquivo) {
        Time entity = buscarEValidarAdmin(id);
        validarImagem(arquivo);

        entity.setLogo(lerBytes(arquivo));
        entity.setLogoContentType(arquivo.getContentType());
        repository.save(entity);

        log.info("Logo atualizado: timeId={}", id);
    }

    @Transactional(readOnly = true)
    public Imagem buscarLogo(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (entity.getLogo() == null) {
            throw new ResourceNotFoundException("Time não tem logo");
        }

        return new Imagem(entity.getLogo(), entity.getLogoContentType());
    }

    private Time buscarEValidarAdmin(UUID id) {
        Time entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado: " + id));

        if (!platformOwnerGuard.isOwner()) {
            authenticatedUserProvider.validarAdminDoTime(id);
        }

        return entity;
    }

    private void validarImagem(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Envie um arquivo de imagem.");
        }
        if (!TIPOS_IMAGEM_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new BusinessException("Formato de imagem não suportado. Use PNG, JPEG ou WEBP.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_IMAGEM_BYTES) {
            throw new BusinessException("Imagem muito grande. Tamanho máximo: 2MB.");
        }
    }

    private byte[] lerBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Erro ao processar a imagem enviada.");
        }
    }

}
