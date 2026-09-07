package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.ConviteRegistrarRequest;
import com.futsalmanager.api.dto.request.RegisterRequest;
import com.futsalmanager.api.dto.response.ConviteInfoResponse;
import com.futsalmanager.api.dto.response.LoginResponse;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.enums.PerfilUsuario;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração com PostgreSQL via TestContainers para /api/time/convite.
 * NOTA: Requer Docker instalado e em execução.
 */
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConviteControllerPostgresIntegrationTest extends AbstractTestcontainersTest {

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
    void buscarConvite_ComCodigoValido_DeveRetornarNomeDoTime() throws Exception {
        String codigo = criarTimeERetornarCodigo("Time do Convite");

        mockMvc.perform(get("/api/time/convite/{codigo}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeTime").value("Time do Convite"));
    }

    @Test
    void buscarConvite_ComCodigoInexistente_DeveRetornar404() throws Exception {
        mockMvc.perform(get("/api/time/convite/{codigo}", "codigo-que-nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrar_ComEmailNovo_DeveCriarIdentidadeEEntrarComoAtleta() throws Exception {
        String codigo = criarTimeERetornarCodigo("Time A");
        String email = "atleta-" + UUID.randomUUID() + "@email.com";

        LoginResponse resposta = registrarViaConvite(codigo, "Novo Atleta", email, "senha123");

        assertThat(resposta.times()).hasSize(1);
        assertThat(resposta.times().get(0).perfil()).isEqualTo(PerfilUsuario.ATLETA);
        assertThat(usuarioTimeRepository.findByUsuarioIdAndAtivoTrue(resposta.usuarioId())).hasSize(1);
    }

    @Test
    void registrar_ComEmailJaExistenteESenhaCorreta_DeveEntrarNoTimeComAMesmaIdentidade() throws Exception {
        String email = "duplo-convite-" + UUID.randomUUID() + "@email.com";
        RegisterRequest registro = new RegisterRequest("Time Original", BigDecimal.valueOf(50), "Fulano", email, "senha123");
        MvcResult registroResult = mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro))
        ).andExpect(status().isCreated()).andReturn();
        LoginResponse original = objectMapper.readValue(registroResult.getResponse().getContentAsString(), LoginResponse.class);

        String codigoConvite = criarTimeERetornarCodigo("Time Convidado");

        LoginResponse resposta = registrarViaConvite(codigoConvite, "Fulano", email, "senha123");

        assertThat(resposta.usuarioId()).isEqualTo(original.usuarioId());
        assertThat(resposta.times()).hasSize(2);
        assertThat(usuarioRepository.findByEmail(email)).hasValueSatisfying(u -> assertThat(u.getId()).isEqualTo(original.usuarioId()));
    }

    @Test
    void registrar_ComEmailJaExistenteESenhaErrada_DeveRejeitar() throws Exception {
        String email = "protegido-convite-" + UUID.randomUUID() + "@email.com";
        RegisterRequest registro = new RegisterRequest("Time Original", BigDecimal.valueOf(50), "Fulano", email, "senha-correta");
        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro))
        ).andExpect(status().isCreated());

        String codigoConvite = criarTimeERetornarCodigo("Time Convidado");
        ConviteRegistrarRequest request = new ConviteRegistrarRequest("Fulano", email, "senha-errada");

        mockMvc.perform(
                post("/api/time/convite/{codigo}/registrar", codigoConvite)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.mensagem", containsString("senha")));
    }

    @Test
    void registrar_QuandoJaMembroDoTime_DeveRejeitar() throws Exception {
        String codigo = criarTimeERetornarCodigo("Time A");
        String email = "repetido-" + UUID.randomUUID() + "@email.com";

        registrarViaConvite(codigo, "Atleta", email, "senha123");

        ConviteRegistrarRequest request = new ConviteRegistrarRequest("Atleta", email, "senha123");
        mockMvc.perform(
                post("/api/time/convite/{codigo}/registrar", codigo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.mensagem", containsString("já é membro")));
    }

    private String criarTimeERetornarCodigo(String nomeTime) throws Exception {
        String emailAdmin = "admin-" + UUID.randomUUID() + "@email.com";
        RegisterRequest registro = new RegisterRequest(nomeTime, BigDecimal.valueOf(50), "Admin", emailAdmin, "senha123");
        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro))
        ).andExpect(status().isCreated());

        Time time = timeRepository.findAll().stream()
                .filter(t -> t.getNome().equals(nomeTime))
                .findFirst()
                .orElseThrow();
        return time.getCodigo();
    }

    private LoginResponse registrarViaConvite(String codigo, String nome, String email, String senha) throws Exception {
        ConviteRegistrarRequest request = new ConviteRegistrarRequest(nome, email, senha);

        MvcResult result = mockMvc.perform(
                post("/api/time/convite/{codigo}/registrar", codigo)
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
