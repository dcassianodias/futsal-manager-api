package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.TimeCreateRequest;
import com.futsalmanager.api.dto.request.TimeUpdateRequest;
import com.futsalmanager.api.dto.response.TimeResponse;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.testcontainers.AbstractTestcontainersTest;
import com.futsalmanager.testcontainers.DockerAvailableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@ExtendWith(DockerAvailableCondition.class)
class TimeControllerIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    private UUID timeId;
    private TimeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        timeRepository.deleteAll();

        createRequest = new TimeCreateRequest(
            "Time de Integração",
            BigDecimal.valueOf(75.50)
        );
    }

    @Test
    void create_DeveCriarTime_ComDadosValidos() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
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

        mockMvc.perform(
            get("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
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
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void findAll_DeveRetornarTodosTimes() throws Exception {
        criarTime();
        criarTime();

        mockMvc.perform(
            get("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].nome", is("Time de Integração")))
        .andExpect(jsonPath("$[1].nome", is("Time de Integração")));
    }

    @Test
    void update_DeveAtualizarTime_ComDadosValidos() throws Exception {
        TimeResponse created = criarTime();

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
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome", is("Time Atualizado")))
        .andExpect(jsonPath("$.valorMensalidade", is(100.00)));
    }

    @Test
    void desativar_DeveDesativarTime() throws Exception {
        TimeResponse created = criarTime();

        mockMvc.perform(
            patch("/api/time/v1/{id}/desativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());

        mockMvc.perform(
            get("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo", is(false)));
    }

    @Test
    void ativar_DeveAtivarTime_QuandoDesativado() throws Exception {
        TimeResponse created = criarTime();

        // Desativar
        mockMvc.perform(
            patch("/api/time/v1/{id}/desativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());

        // Ativar
        mockMvc.perform(
            patch("/api/time/v1/{id}/ativar", created.id())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());

        mockMvc.perform(
            get("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
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
        )
        .andExpect(status().isBadRequest());
    }

    private TimeResponse criarTime() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, TimeResponse.class);
    }
}
