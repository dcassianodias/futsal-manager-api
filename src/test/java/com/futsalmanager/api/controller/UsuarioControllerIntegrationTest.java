package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import com.futsalmanager.testcontainers.AbstractTestcontainersTest;
import com.futsalmanager.testcontainers.DockerAvailableCondition;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do UsuarioController — agora restrito a identidade
 * (buscar/listar/atualizar o próprio usuário). Fluxos de vínculo com time
 * (adicionar/remover/trocar perfil de membro) estão em
 * {@link UsuarioControllerPostgresIntegrationTest}, contra o
 * UsuarioTimeController.
 */
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.platform-owner-email=dono-teste@integration.com")
class UsuarioControllerIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioTimeRepository usuarioTimeRepository;

    private Time time;
    private Usuario usuarioLogado;
    private Authentication currentAuth;

    @BeforeEach
    void setUp() {
        usuarioTimeRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();

        time = new Time(null, "Time Teste", BigDecimal.valueOf(50.00), true, null, null);
        time = timeRepository.save(time);

        usuarioLogado = criarUsuarioPersistido("joao@email.com", "João Silva");
        vincular(usuarioLogado, time, PerfilUsuario.ADMIN);

        autenticarComo(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        usuarioTimeRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();
    }

    private Usuario criarUsuarioPersistido(String email, String nome) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("senha-hash");
        usuario.setAtivo(true);
        usuario.setGols(0);
        return usuarioRepository.save(usuario);
    }

    private void vincular(Usuario usuario, Time time, PerfilUsuario perfil) {
        UsuarioTime vinculo = new UsuarioTime();
        vinculo.setUsuario(usuario);
        vinculo.setTime(time);
        vinculo.setPerfil(perfil);
        vinculo.setAtivo(true);
        usuarioTimeRepository.save(vinculo);
    }

    private void autenticarComo(Usuario usuario) {
        currentAuth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor auth() {
        return request -> authentication(currentAuth).postProcessRequest(request);
    }

    @Test
    void findById_DeveRetornarUsuario_QuandoExiste() throws Exception {
        mockMvc.perform(
            get("/api/usuario/v1/{id}", usuarioLogado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(usuarioLogado.getId().toString())))
        .andExpect(jsonPath("$.nome", is("João Silva")))
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void findById_DeveRetornar404_QuandoNaoExiste() throws Exception {
        UUID inexistenteId = UUID.randomUUID();

        mockMvc.perform(
            get("/api/usuario/v1/{id}", inexistenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void findAll_DeveSerNegado_QuandoNaoForDonoDaPlataforma() throws Exception {
        mockMvc.perform(
            get("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void findAll_DeveRetornarTodosUsuariosAtivos_QuandoDonoDaPlataforma() throws Exception {
        criarUsuarioPersistido("outro@email.com", "Outro Usuário");

        Usuario dono = criarUsuarioPersistido("dono-teste@integration.com", "Dono da Plataforma");
        autenticarComo(dono);

        mockMvc.perform(
            get("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void update_DeveAtualizarOProprioPerfil_ComDadosValidos() throws Exception {
        UsuarioUpdateRequest updateRequest = new UsuarioUpdateRequest(
            "João Silva Atualizado",
            "joao.novo@email.com",
            "654321"
        );

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(
            patch("/api/usuario/v1/{id}", usuarioLogado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome", is("João Silva Atualizado")))
        .andExpect(jsonPath("$.email", is("joao.novo@email.com")));
    }

    @Test
    void update_DeveSerNegado_QuandoTentaEditarOutroUsuario() throws Exception {
        Usuario outro = criarUsuarioPersistido("outro2@email.com", "Outro Usuário");

        UsuarioUpdateRequest updateRequest = new UsuarioUpdateRequest("Hackeado", null, null);
        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(
            patch("/api/usuario/v1/{id}", outro.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isForbidden());
    }
}
