package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.testcontainers.AbstractTestcontainersTest;
import com.futsalmanager.testcontainers.DockerAvailableCondition;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração com PostgreSQL via TestContainers para Usuario.
 * Testa persistência real e comportamento com banco de dados.
 *
 * NOTA: Requer Docker instalado e em execução.
 * Se Docker não estiver disponível, estes testes serão ignorados.
 */
@AutoConfigureMockMvc
@Transactional
@ExtendWith(DockerAvailableCondition.class)
class UsuarioControllerPostgresIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UUID timeId;
    private Time time;
    private UsuarioCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();

        // Criar um time para os testes
        time = new Time(null, "Time para Postgres", BigDecimal.valueOf(50.00), true, null, null);
        time = timeRepository.save(time);
        timeId = time.getId();

        autenticarComo(time, PerfilUsuario.ADMIN);

        createRequest = new UsuarioCreateRequest(
            timeId,
            "Maria Silva",
            "maria@email.com",
            "senha123",
            null
        );
    }

    private Authentication currentAuth;

    /**
     * Troca qual usuário fica autenticado nas próximas chamadas ao MockMvc
     * (use junto com auth(), aplicado em cada .perform(...)).
     */
    private void autenticarComo(Time time, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Usuário de Teste");
        usuario.setEmail("teste-auth@email.com");
        usuario.setSenha("senha");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setTime(time);

        currentAuth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
    }

    /**
     * RequestPostProcessor que injeta o usuário autenticado atual na
     * requisição - setar o SecurityContextHolder direto não sobrevive
     * ao SecurityContextHolderFilter entre chamadas separadas do MockMvc.
     */
    private RequestPostProcessor auth() {
        return request -> authentication(currentAuth).postProcessRequest(request);
    }

    @Test
    void create_DevePersistirUsuarioNoPostgreSQL() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.nome", is("Maria Silva")))
        .andReturn();

        String response = result.getResponse().getContentAsString();
        UsuarioResponse usuarioResponse = objectMapper.readValue(response, UsuarioResponse.class);

        // Verificar persistência no PostgreSQL
        assertThat(usuarioRepository.findById(usuarioResponse.id())).isPresent();
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void reativacao_DeveManterHistoricoNoPostgreSQL() throws Exception {
        // Criar usuário
        UsuarioResponse created = criarUsuario();

        // Desativar
        mockMvc.perform(
            delete("/api/usuario/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNoContent());

        // Verificar que foi desativado
        var usuarioOpt = usuarioRepository.findById(created.id());
        assertThat(usuarioOpt).isPresent();
        assertThat(usuarioOpt.get().isAtivo()).isFalse();

        // Reativar
        mockMvc.perform(
            patch("/api/usuario/v1/{id}/reativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void emailUniqueness_DeveEnforcarRestricaoNoBanco() throws Exception {
        // Criar primeiro usuário
        criarUsuarioComEmail("joao@email.com");

        // Tentar criar segundo usuário com mesmo email
        UsuarioCreateRequest request2 = new UsuarioCreateRequest(
            timeId,
            "Outro João",
            "joao@email.com",
            "senha456",
            null
        );

        String json = objectMapper.writeValueAsString(request2);
        mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isBadRequest());

        // Verificar que apenas um foi criado
        assertThat(usuarioRepository.findAll()).hasSize(1);
    }

    @Test
    void findByTime_DeveRetornarApenasUsuariosDoTime() throws Exception {
        // Criar outro time
        Time outroTime = new Time(null, "Outro Time", BigDecimal.valueOf(75.00), true, null, null);
        outroTime = timeRepository.save(outroTime);

        // Criar usuários em times diferentes
        criarUsuarioComEmail("usuario1@email.com");

        UsuarioCreateRequest request2 = new UsuarioCreateRequest(
            outroTime.getId(),
            "João do Outro Time",
            "usuario2@email.com",
            "senha789",
            null
        );

        // precisa estar autenticado como admin do outro time para criar usuário nele
        autenticarComo(outroTime, PerfilUsuario.ADMIN);

        String json = objectMapper.writeValueAsString(request2);
        mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated());

        // volta a autenticar como admin do time original para consultar
        autenticarComo(time, PerfilUsuario.ADMIN);

        // Buscar usuários por time
        mockMvc.perform(
            get("/api/usuario/v1/time/{timeId}", timeId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].timeId", is(timeId.toString())));

        // Verificar no banco que temos 2 usuários e 2 times
        assertThat(usuarioRepository.findAll()).hasSize(2);
        assertThat(timeRepository.findAll()).hasSize(2);
    }

    @Test
    void concorrencia_DeveGerenciarMultiplosUsuariosDoMesmoTime() throws Exception {
        // Criar múltiplos usuários do mesmo time
        for (int i = 1; i <= 5; i++) {
            criarUsuarioComEmail("usuario" + i + "@email.com");
        }

        // Verificar persistência
        assertThat(usuarioRepository.findAll()).hasSize(5);
    }

    private UsuarioResponse criarUsuario() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, UsuarioResponse.class);
    }

    private UsuarioResponse criarUsuarioComEmail(String email) throws Exception {
        UsuarioCreateRequest request = new UsuarioCreateRequest(
            timeId,
            "Usuario Teste",
            email,
            "senha123",
            null
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, UsuarioResponse.class);
    }
}

