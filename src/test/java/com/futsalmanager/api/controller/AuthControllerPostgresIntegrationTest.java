package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.LoginRequest;
import com.futsalmanager.api.dto.request.RegisterRequest;
import com.futsalmanager.api.dto.response.LoginResponse;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração com PostgreSQL via TestContainers para /auth.
 * NOTA: Requer Docker instalado e em execução.
 */
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerPostgresIntegrationTest extends AbstractTestcontainersTest {

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

    @BeforeEach
    void setUp() {
        usuarioTimeRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();
    }

    @Test
    void register_ComEmailNovo_DeveCriarIdentidadeETime() throws Exception {
        String email = "novo-" + UUID.randomUUID() + "@email.com";

        LoginResponse resposta = registrar(email, "senha123", "Time A");

        assertThat(resposta.times()).hasSize(1);
        assertThat(resposta.times().get(0).timeNome()).isEqualTo("Time A");
        assertThat(usuarioRepository.findByEmail(email)).isPresent();
    }

    @Test
    void register_ComEmailJaExistenteESenhaCorreta_DeveReaproveitarIdentidadeECriarSegundoTime() throws Exception {
        String email = "duplo-" + UUID.randomUUID() + "@email.com";

        LoginResponse primeiro = registrar(email, "senha123", "Time A");
        LoginResponse segundo = registrar(email, "senha123", "Time B");

        // Mesma identidade (mesmo usuarioId), agora com dois vínculos.
        assertThat(segundo.usuarioId()).isEqualTo(primeiro.usuarioId());
        assertThat(segundo.times()).hasSize(2);
        assertThat(segundo.times()).extracting(t -> t.timeNome())
                .containsExactlyInAnyOrder("Time A", "Time B");
        assertThat(usuarioRepository.findAll()).hasSize(1);
    }

    @Test
    void register_ComEmailJaExistenteESenhaErrada_DeveRejeitar() throws Exception {
        String email = "protegido-" + UUID.randomUUID() + "@email.com";

        registrar(email, "senha-correta", "Time A");

        RegisterRequest request = new RegisterRequest("Time B", BigDecimal.valueOf(50), "Alguém", email, "senha-errada");

        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.mensagem", containsString("senha")));

        // Não deve ter criado um segundo time nem duplicado o usuário.
        assertThat(timeRepository.findAll()).hasSize(1);
        assertThat(usuarioRepository.findAll()).hasSize(1);
    }

    @Test
    void login_ComUsuarioComDoisTimes_DeveRetornarAmbosOsVinculos() throws Exception {
        String email = "logintime-" + UUID.randomUUID() + "@email.com";
        registrar(email, "senha123", "Time A");
        registrar(email, "senha123", "Time B");

        LoginRequest loginRequest = new LoginRequest(email, "senha123");

        MvcResult result = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        )
        .andExpect(status().isOk())
        .andReturn();

        LoginResponse resposta = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        assertThat(resposta.times()).hasSize(2);
    }

    private LoginResponse registrar(String email, String senha, String nomeTime) throws Exception {
        RegisterRequest request = new RegisterRequest(nomeTime, BigDecimal.valueOf(50), "Admin Teste", email, senha);

        MvcResult result = mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
    }

    @AfterEach
    void tearDown() {
        usuarioTimeRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();
    }
}
