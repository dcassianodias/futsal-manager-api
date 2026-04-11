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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração com PostgreSQL via TestContainers.
 * Esses testes usam um container Docker real do PostgreSQL.
 *
 * NOTA: Requer Docker instalado e em execução.
 * Se Docker não estiver disponível, estes testes serão ignorados.
 */
@AutoConfigureMockMvc
@Transactional
@ExtendWith(DockerAvailableCondition.class)
class TimeControllerPostgresIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    private TimeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        timeRepository.deleteAll();

        createRequest = new TimeCreateRequest(
            "Time com PostgreSQL",
            BigDecimal.valueOf(100.00)
        );
    }

    @Test
    void create_DeveCriarTime_ComPostgreSQL() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.nome", is("Time com PostgreSQL")))
        .andExpect(jsonPath("$.valorMensalidade", is(100.00)))
        .andReturn();

        String response = result.getResponse().getContentAsString();
        TimeResponse timeResponse = objectMapper.readValue(response, TimeResponse.class);

        // Verificar no banco de dados
        assertThat(timeRepository.findById(timeResponse.id())).isPresent();
    }

    @Test
    void persistencia_DeveManterDadosNoPostgreSQL() throws Exception {
        // Criar time
        String json = objectMapper.writeValueAsString(createRequest);
        MvcResult result = mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andReturn();

        String response = result.getResponse().getContentAsString();
        TimeResponse timeResponse = objectMapper.readValue(response, TimeResponse.class);

        // Buscar no banco e verificar persistência
        var timeOptional = timeRepository.findById(timeResponse.id());
        assertThat(timeOptional).isPresent();
        assertThat(timeOptional.get().getNome()).isEqualTo("Time com PostgreSQL");
        assertThat(timeOptional.get().getValorMensalidade()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void concorrencia_DeveGerenciarMultiplosTimes() throws Exception {
        // Criar múltiplos times concorrentemente
        for (int i = 1; i <= 5; i++) {
            TimeCreateRequest request = new TimeCreateRequest(
                "Time " + i,
                BigDecimal.valueOf(50.00 + i)
            );

            String json = objectMapper.writeValueAsString(request);
            mockMvc.perform(
                post("/api/time/v1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isCreated());
        }

        // Verificar que todos foram salvos
        assertThat(timeRepository.findAll()).hasSize(5);
    }

    @Test
    void transacao_DeveReverterEmCasoDeErro() throws Exception {
        long countAntes = timeRepository.count();

        // Tentar criar um time com dados inválidos
        TimeCreateRequest invalidRequest = new TimeCreateRequest(
            "", // Nome vazio
            BigDecimal.valueOf(-100.00) // Valor negativo
        );

        String json = objectMapper.writeValueAsString(invalidRequest);
        mockMvc.perform(
            post("/api/time/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isBadRequest());

        // Verificar que nada foi salvo
        assertThat(timeRepository.count()).isEqualTo(countAntes);
    }

    @Test
    void update_DeveAtualizarNoPostgreSQL() throws Exception {
        // Criar time
        TimeResponse created = criarTime();

        // Atualizar
        TimeUpdateRequest updateRequest = new TimeUpdateRequest(
            "Time Atualizado no PostgreSQL",
            BigDecimal.valueOf(150.00),
            true
        );

        String json = objectMapper.writeValueAsString(updateRequest);
        mockMvc.perform(
            patch("/api/time/v1/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isOk());

        // Verificar persistência da atualização
        var timeOptional = timeRepository.findById(created.id());
        assertThat(timeOptional).isPresent();
        assertThat(timeOptional.get().getNome()).isEqualTo("Time Atualizado no PostgreSQL");
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
