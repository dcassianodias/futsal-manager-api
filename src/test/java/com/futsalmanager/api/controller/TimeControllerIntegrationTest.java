package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.platform-owner-email=dono-teste@integration.com")
class TimeControllerIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    private UUID timeId;

    private TimeCreateRequest createRequest;

    private Authentication currentAuth;

    @BeforeEach
    void setUp() {
        timeRepository.deleteAll();

        createRequest = new TimeCreateRequest(
            "Time de Integração",
            BigDecimal.valueOf(75.50)
        );

        autenticarComoTime(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        timeRepository.deleteAll();
    }

    /**
     * Troca qual usuário fica autenticado nas próximas chamadas ao MockMvc
     * (use junto com auth(), aplicado em cada .perform(...)). O time não
     * precisa existir no banco - nada relê o principal a partir dele.
     */
    private void autenticarComoTime(UUID timeId) {
        Time time = new Time(timeId, null, null, null, null, null);
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Usuário de Teste");
        usuario.setEmail("teste-auth@email.com");
        usuario.setSenha("senha");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setTime(time);

        currentAuth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
    }

    /** Autentica como o dono da plataforma (configurado via app.platform-owner-email). */
    private void autenticarComoDono() {
        Time time = new Time(UUID.randomUUID(), null, null, null, null, null);
        Usuario dono = new Usuario();
        dono.setId(UUID.randomUUID());
        dono.setNome("Dono da Plataforma");
        dono.setEmail("dono-teste@integration.com");
        dono.setSenha("senha");
        dono.setPerfil(PerfilUsuario.ADMIN);
        dono.setAtivo(true);
        dono.setTime(time);

        currentAuth = new UsernamePasswordAuthenticationToken(dono, null, dono.getAuthorities());
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
    void create_DeveCriarTime_ComDadosValidos() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.nome", is("Time de Integração")))
        .andExpect(jsonPath("$.valorMensalidade", is(75.50)))
        .andExpect(jsonPath("$.ativo", is(true)))
        .andReturn();

        String response = result.getResponse().getContentAsString();
        TimeResponse timeResponse = objectMapper.readValue(response, TimeResponse.class);
        timeId = timeResponse.id();

        assertThat(timeId).isNotNull();
        assertThat(timeRepository.count()).isEqualTo(1);
    }

    @Test
    void findById_DeveRetornarTime_QuandoExiste() throws Exception {
        TimeResponse created = criarTime();
        autenticarComoTime(created.id());

        mockMvc.perform(
            get("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(created.id().toString())))
        .andExpect(jsonPath("$.nome", is("Time de Integração")))
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void findById_DeveRetornar404_QuandoNaoExiste() throws Exception {
        UUID inexistenteId = UUID.randomUUID();

        mockMvc.perform(
            get("/api/time/v1/{id}", inexistenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void findAll_DeveRetornarTodosTimes() throws Exception {
        criarTime();
        criarTime();
        autenticarComoDono();

        mockMvc.perform(
            get("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].nome", is("Time de Integração")))
        .andExpect(jsonPath("$[1].nome", is("Time de Integração")));
    }

    @Test
    void update_DeveAtualizarTime_ComDadosValidos() throws Exception {
        TimeResponse created = criarTime();
        autenticarComoTime(created.id());

        TimeUpdateRequest updateRequest = new TimeUpdateRequest(
            "Time Atualizado",
            BigDecimal.valueOf(100.00),
            true
        );

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(
            patch("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome", is("Time Atualizado")))
        .andExpect(jsonPath("$.valorMensalidade", is(100.00)));
    }

    @Test
    void desativar_DeveDesativarTime() throws Exception {
        TimeResponse created = criarTime();
        autenticarComoTime(created.id());

        mockMvc.perform(
            patch("/api/time/v1/{id}/desativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNoContent());

        mockMvc.perform(
            get("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo", is(false)));
    }

    @Test
    void ativar_DeveAtivarTime_QuandoDesativado() throws Exception {
        TimeResponse created = criarTime();
        autenticarComoTime(created.id());

        // Desativar
        mockMvc.perform(
            patch("/api/time/v1/{id}/desativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNoContent());

        // Ativar
        mockMvc.perform(
            patch("/api/time/v1/{id}/ativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNoContent());

        mockMvc.perform(
            get("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void create_DeveRetornarErro400_QuandoDadosInvalidos() throws Exception {
        TimeCreateRequest invalidRequest = new TimeCreateRequest(
            "", // Nome vazio
            BigDecimal.valueOf(-10.00) // Valor negativo
        );

        String json = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isBadRequest());
    }

    private TimeResponse criarTime() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, TimeResponse.class);
    }
}
