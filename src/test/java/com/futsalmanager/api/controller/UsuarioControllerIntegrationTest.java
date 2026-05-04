package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.UsuarioCreateRequest;
import com.futsalmanager.api.dto.request.UsuarioUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.api.dto.response.UsuarioResponse;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.testcontainers.AbstractTestcontainersTest;
import com.futsalmanager.testcontainers.DockerAvailableCondition;
import com.futsalmanager.domain.entities.Time;
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

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioControllerIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UUID timeId;
    private UsuarioCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();

        // Criar um time para ser usado nos testes
        Time time = new Time(null, "Time Teste", BigDecimal.valueOf(50.00), true, null, null);
        time = timeRepository.save(time);
        timeId = time.getId();

        createRequest = new UsuarioCreateRequest(
            timeId,
            "João Silva",
            "joao@email.com",
            "123456"
        );
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();
    }

    @Test
    void create_DeveCriarUsuario_ComDadosValidos() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.nome", is("João Silva")))
        .andExpect(jsonPath("$.email", is("joao@email.com")))
        .andExpect(jsonPath("$.ativo", is(true)))
        .andReturn();

        String response = result.getResponse().getContentAsString();
        UsuarioResponse usuarioResponse = objectMapper.readValue(response, UsuarioResponse.class);

        assertThat(usuarioResponse.id()).isNotNull();
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void findById_DeveRetornarUsuario_QuandoExiste() throws Exception {
        UsuarioResponse created = criarUsuario();

        mockMvc.perform(
            get("/api/usuario/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(created.id().toString())))
        .andExpect(jsonPath("$.nome", is("João Silva")))
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void findById_DeveRetornar404_QuandoNaoExiste() throws Exception {
        UUID inexistenteId = UUID.randomUUID();

        mockMvc.perform(
            get("/api/usuario/v1/{id}", inexistenteId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void findAll_DeveRetornarTodosUsuariosAtivos() throws Exception {
        criarUsuarioComEmail("joao1@email.com");
        criarUsuarioComEmail("joao2@email.com");

        mockMvc.perform(
            get("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].nome", is("João Silva")))
        .andExpect(jsonPath("$[1].nome", is("João Silva")));
    }

    @Test
    void findByTimeId_DeveRetornarUsuariosDoTime() throws Exception {
        criarUsuario();

        mockMvc.perform(
            get("/api/usuario/v1/time/{timeId}", timeId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].timeId", is(timeId.toString())));
    }

    @Test
    void update_DeveAtualizarUsuario_ComDadosValidos() throws Exception {
        UsuarioResponse created = criarUsuario();

        UsuarioUpdateRequest updateRequest = new UsuarioUpdateRequest(
            "João Silva Atualizado",
            "joao.novo@email.com",
            "654321"
        );

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(
            patch("/api/usuario/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome", is("João Silva Atualizado")))
        .andExpect(jsonPath("$.email", is("joao.novo@email.com")));
    }

    @Test
    void delete_DeveDesativarUsuario() throws Exception {
        UsuarioResponse created = criarUsuario();

        mockMvc.perform(
            delete("/api/usuario/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());

        mockMvc.perform(
            get("/api/usuario/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void reativar_DeveReativarUsuario_QuandoDesativado() throws Exception {
        UsuarioResponse created = criarUsuario();

        // Desativar
        mockMvc.perform(
            delete("/api/usuario/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());

        // Reativar
        mockMvc.perform(
            patch("/api/usuario/v1/{id}/reativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void create_DeveRetornarErro400_QuandoDadosInvalidos() throws Exception {
        UsuarioCreateRequest invalidRequest = new UsuarioCreateRequest(
            UUID.randomUUID(),
            "", // Nome vazio
            "email-invalido", // Email inválido
            "123" // Senha muito curta
        );

        String json = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void create_DeveRetornarErro400_QuandoTimeNaoExiste() throws Exception {
        UsuarioCreateRequest requestComTimeInvalido = new UsuarioCreateRequest(
            UUID.randomUUID(),
            "João Silva",
            "joao@email.com",
            "123456"
        );

        String json = objectMapper.writeValueAsString(requestComTimeInvalido);

        mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isNotFound());
    }

    private UsuarioResponse criarUsuario() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, UsuarioResponse.class);
    }

    private UsuarioResponse criarUsuarioComEmail(String email) throws Exception {
        UsuarioCreateRequest request = new UsuarioCreateRequest(
            timeId,
            "João Silva",
            email,
            "123456"
        );
        
        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(
            post("/api/usuario/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, UsuarioResponse.class);
    }
}
