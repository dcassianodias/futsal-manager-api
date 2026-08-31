package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.application.mappers.UsuarioMapper;
import com.futsalmanager.application.validators.UsuarioValidator;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private UsuarioValidator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private UsuarioService usuarioService;

    private UUID usuarioId;
    private Usuario usuario;
    private UsuarioResponse usuarioResponse;
    private UsuarioUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("123456");
        usuario.setAtivo(true);
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());

        usuarioResponse = new UsuarioResponse(
            usuarioId,
            "João Silva",
            "joao@email.com",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        updateRequest = new UsuarioUpdateRequest(
            "João Silva Atualizado",
            "joao.atualizado@email.com",
            "654321"
        );

        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void findById_DeveRetornarUsuario_QuandoUsuarioAtivoExiste() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        UsuarioResponse result = usuarioService.findById(usuarioId);

        assertThat(result).isEqualTo(usuarioResponse);
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioMapper).toResponse(usuario);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void findById_DeveLancarResourceNotFoundException_QuandoUsuarioInativo() {
        usuario.setAtivo(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.findById(usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void findAll_DeveRetornarListaDeUsuariosAtivos() {
        List<Usuario> usuarios = List.of(usuario);
        List<UsuarioResponse> responses = List.of(usuarioResponse);

        when(usuarioRepository.findByAtivoTrue()).thenReturn(usuarios);
        when(usuarioMapper.toResponseList(usuarios)).thenReturn(responses);

        List<UsuarioResponse> result = usuarioService.findAll();

        assertThat(result).isEqualTo(responses);
        verify(usuarioRepository).findByAtivoTrue();
        verify(usuarioMapper).toResponseList(usuarios);
    }

    @Test
    void update_DeveAtualizarUsuario_QuandoEditandoOProprioPerfil() {
        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(usuario);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        doNothing().when(validator).validarUpdate(eq(usuario), eq(updateRequest));
        doNothing().when(usuarioMapper).updateEntityFromRequest(eq(updateRequest), eq(usuario));

        UsuarioResponse result = usuarioService.update(usuarioId, updateRequest);

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
        UsuarioUpdateRequest partialUpdate = new UsuarioUpdateRequest("Novo Nome", null, null);

        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(usuario);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        doNothing().when(validator).validarUpdate(eq(usuario), eq(partialUpdate));
        doNothing().when(usuarioMapper).updateEntityFromRequest(eq(partialUpdate), eq(usuario));

        UsuarioResponse result = usuarioService.update(usuarioId, partialUpdate);

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
    void update_DeveLancarAccessDeniedException_QuandoNaoForOProprioPerfil() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());

        when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(outroUsuario);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.update(usuarioId, updateRequest))
            .isInstanceOf(AccessDeniedException.class);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(validator, usuarioMapper);
    }

    @Test
    void update_DeveLancarResourceNotFoundException_QuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.update(usuarioId, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Usuário não encontrado: " + usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verifyNoInteractions(validator, usuarioMapper);
    }
}
