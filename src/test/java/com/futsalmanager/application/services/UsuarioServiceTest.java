package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.UsuarioMapper;
import com.futsalmanager.application.validators.UsuarioValidator;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private UsuarioValidator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private UsuarioService usuarioService;

    private UUID usuarioId;
    private UUID timeId;
    private Usuario usuario;
    private Usuario adminLogado;
    private Time time;
    private UsuarioResponse usuarioResponse;
    private UsuarioCreateRequest createRequest;
    private UsuarioUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        timeId = UUID.randomUUID();

        time = new Time(timeId, "Time Teste", null, true, LocalDateTime.now(), LocalDateTime.now());

        adminLogado = new Usuario();
        adminLogado.setId(UUID.randomUUID());
        adminLogado.setPerfil(PerfilUsuario.ADMIN);
        adminLogado.setTime(time);

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("123456");
        usuario.setPerfil(PerfilUsuario.ATLETA);
        usuario.setAtivo(true);
        usuario.setTime(time);
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());

        usuarioResponse = new UsuarioResponse(
            usuarioId,
            "João Silva",
            "joao@email.com",
            timeId,
            PerfilUsuario.ATLETA,
            true,
            0,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        createRequest = new UsuarioCreateRequest(
            timeId,
            "João Silva",
            "joao@email.com",
            "123456",
            null
        );

        updateRequest = new UsuarioUpdateRequest(
            "João Silva Atualizado",
            "joao.atualizado@email.com",
            "654321",
            null
        );

        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void findById_DeveRetornarUsuario_QuandoUsuarioAtivoExiste() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        // Act
        UsuarioResponse result = usuarioService.findById(usuarioId);

        // Assert
        assertThat(result).isEqualTo(usuarioResponse);
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioMapper).toResponse(usuario);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoUsuarioNaoExiste() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.findById(usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoUsuarioInativo() {
        // Arrange
        usuario.setAtivo(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.findById(usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void findAll_DeveRetornarListaDeUsuariosAtivos() {
        // Arrange
        List<Usuario> usuarios = List.of(usuario);
        List<UsuarioResponse> responses = List.of(usuarioResponse);

        when(usuarioRepository.findByAtivoTrue()).thenReturn(usuarios);
        when(usuarioMapper.toResponseList(usuarios)).thenReturn(responses);

        // Act
        List<UsuarioResponse> result = usuarioService.findAll();

        // Assert
        assertThat(result).isEqualTo(responses);
        verify(usuarioRepository).findByAtivoTrue();
        verify(usuarioMapper).toResponseList(usuarios);
    }

    @Test
    void findByTimeId_DeveRetornarUsuariosDoTime() {
        // Arrange
        List<Usuario> usuarios = List.of(usuario);
        List<UsuarioResponse> responses = List.of(usuarioResponse);

        when(usuarioRepository.findByTimeIdAndAtivoTrue(timeId)).thenReturn(usuarios);
        when(usuarioMapper.toResponseList(usuarios)).thenReturn(responses);

        // Act
        List<UsuarioResponse> result = usuarioService.findByTimeId(timeId);

        // Assert
        assertThat(result).isEqualTo(responses);
        verify(usuarioRepository).findByTimeIdAndAtivoTrue(timeId);
        verify(usuarioMapper).toResponseList(usuarios);
    }

    @Test
    void create_DeveCriarNovoUsuario_QuandoDadosValidos() {
        // Arrange
        Usuario novoUsuario = new Usuario();
        novoUsuario.setId(UUID.randomUUID());
        novoUsuario.setNome("João Silva");
        novoUsuario.setEmail("joao@email.com");
        novoUsuario.setSenha("123456");
        novoUsuario.setPerfil(PerfilUsuario.ATLETA);
        novoUsuario.setAtivo(true);
        novoUsuario.setTime(time);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioRepository.findByTimeIdAndEmail(timeId, "joao@email.com")).thenReturn(Optional.empty());
        when(usuarioMapper.toEntity(createRequest)).thenReturn(novoUsuario);
        when(usuarioRepository.save(novoUsuario)).thenReturn(novoUsuario);
        when(usuarioMapper.toResponse(novoUsuario)).thenReturn(usuarioResponse);

        doNothing().when(validator).validarCreate(createRequest);

        // Act
        UsuarioResponse result = usuarioService.create(createRequest);

        // Assert
        assertThat(result).isEqualTo(usuarioResponse);
        verify(validator).validarCreate(createRequest);
        verify(timeRepository).findById(timeId);
        verify(usuarioRepository).findByTimeIdAndEmail(timeId, "joao@email.com");
        verify(usuarioMapper).toEntity(createRequest);
        verify(usuarioRepository).save(novoUsuario);
        verify(usuarioMapper).toResponse(novoUsuario);
    }

    @Test
    void create_DeveReativarUsuarioExistente_QuandoEmailJaCadastradoMasInativo() {
        // Arrange
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(usuarioId);
        usuarioExistente.setNome("João Silva");
        usuarioExistente.setEmail("joao@email.com");
        usuarioExistente.setSenha("oldpassword");
        usuarioExistente.setPerfil(PerfilUsuario.ATLETA);
        usuarioExistente.setAtivo(false);
        usuarioExistente.setTime(time);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioRepository.findByTimeIdAndEmail(timeId, "joao@email.com")).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);
        when(usuarioMapper.toResponse(usuarioExistente)).thenReturn(usuarioResponse);

        doNothing().when(validator).validarCreate(createRequest);

        // Act
        UsuarioResponse result = usuarioService.create(createRequest);

        // Assert
        assertThat(result).isEqualTo(usuarioResponse);
        verify(validator).validarCreate(createRequest);
        verify(timeRepository).findById(timeId);
        verify(usuarioRepository).findByTimeIdAndEmail(timeId, "joao@email.com");
        verify(usuarioRepository).save(usuarioExistente);
        verify(usuarioMapper).toResponse(usuarioExistente);
        assertThat(usuarioExistente.isAtivo()).isTrue();
        assertThat(usuarioExistente.getNome()).isEqualTo("João Silva");
        assertThat(usuarioExistente.getSenha()).isEqualTo("123456");
    }

    @Test
    void create_DeveLancarBusinessException_QuandoEmailJaCadastradoEAtivo() {
        // Arrange
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(usuarioId);
        usuarioExistente.setNome("João Silva");
        usuarioExistente.setEmail("joao@email.com");
        usuarioExistente.setSenha("123456");
        usuarioExistente.setPerfil(PerfilUsuario.ATLETA);
        usuarioExistente.setAtivo(true);
        usuarioExistente.setTime(time);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(usuarioRepository.findByTimeIdAndEmail(timeId, "joao@email.com")).thenReturn(Optional.of(usuarioExistente));

        doNothing().when(validator).validarCreate(createRequest);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.create(createRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Email já cadastrado para este time");

        verify(validator).validarCreate(createRequest);
        verify(timeRepository).findById(timeId);
        verify(usuarioRepository).findByTimeIdAndEmail(timeId, "joao@email.com");
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void create_DeveLancarResourceNotFoundException_QuandoTimeNaoExiste() {
        // Arrange
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        doNothing().when(validator).validarCreate(createRequest);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.create(createRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Time não encontrado: " + timeId);

        verify(validator).validarCreate(createRequest);
        verify(timeRepository).findById(timeId);
        verifyNoMoreInteractions(timeRepository);
        verifyNoInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void update_DeveAtualizarUsuario_QuandoDadosValidos() {
        // Arrange
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(adminLogado);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        doNothing().when(validator).validarUpdate(eq(usuario), eq(updateRequest));
        doNothing().when(usuarioMapper).updateEntityFromRequest(eq(updateRequest), eq(usuario));

        // Act
        UsuarioResponse result = usuarioService.update(usuarioId, updateRequest);

        // Assert
        assertThat(result).isEqualTo(usuarioResponse);
        verify(usuarioRepository).findById(usuarioId);
        verify(validator).validarUpdate(usuario, updateRequest);
        verify(usuarioMapper).updateEntityFromRequest(updateRequest, usuario);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponse(usuario);
        assertThat(usuario.getEmail()).isEqualTo("joao.atualizado@email.com");
        assertThat(usuario.getSenha()).isEqualTo("654321");
    }

    @Test
    void update_DeveAtualizarUsuario_SomenteCamposFornecidos() {
        // Arrange
        UsuarioUpdateRequest partialUpdate = new UsuarioUpdateRequest("Novo Nome", null, null, null);

        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(adminLogado);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        doNothing().when(validator).validarUpdate(eq(usuario), eq(partialUpdate));
        doNothing().when(usuarioMapper).updateEntityFromRequest(eq(partialUpdate), eq(usuario));

        // Act
        UsuarioResponse result = usuarioService.update(usuarioId, partialUpdate);

        // Assert
        assertThat(result).isEqualTo(usuarioResponse);
        verify(usuarioRepository).findById(usuarioId);
        verify(validator).validarUpdate(usuario, partialUpdate);
        verify(usuarioMapper).updateEntityFromRequest(partialUpdate, usuario);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponse(usuario);
        // Email should not be changed since it was null
        assertThat(usuario.getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoUsuarioNaoExiste() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.update(usuarioId, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(validator, usuarioMapper);
    }

    @Test
    void delete_DeveDesativarUsuario_QuandoUsuarioAtivo() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        // Act
        usuarioService.delete(usuarioId);

        // Assert
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioRepository).save(usuario);
        assertThat(usuario.isAtivo()).isFalse();
    }

    @Test
    void delete_DeveLancarResourceNotFoundException_QuandoUsuarioNaoExiste() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.delete(usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void reativar_DeveReativarUsuario_QuandoUsuarioInativo() {
        // Arrange
        usuario.setAtivo(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        // Act
        UsuarioResponse result = usuarioService.reativar(usuarioId);

        // Assert
        assertThat(result).isEqualTo(usuarioResponse);
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponse(usuario);
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void reativar_DeveLancarBusinessException_QuandoUsuarioJaAtivo() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.reativar(usuarioId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Usuário já está ativo");

        verify(usuarioRepository).findById(usuarioId);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void reativar_DeveLancarResourceNotFoundException_QuandoUsuarioNaoExiste() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.reativar(usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(usuarioMapper);
    }
}
