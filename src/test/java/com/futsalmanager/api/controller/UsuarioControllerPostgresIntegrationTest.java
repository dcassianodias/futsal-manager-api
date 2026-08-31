package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.AddMembroRequest;
import com.futsalmanager.api.dto.request.AlterarPerfilMembroRequest;
import com.futsalmanager.api.dto.response.MembroResponse;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.testcontainers.AbstractTestcontainersTest;
import com.futsalmanager.testcontainers.DockerAvailableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
 * Testes de integração com PostgreSQL via TestContainers para o vínculo
 * usuário<->time (UsuarioTimeController), incluindo persistência real do
 * reaproveitamento de identidade por email e das regras de admin único.
 *
 * NOTA: Requer Docker instalado e em execução.
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

    @Autowired
    private UsuarioTimeRepository usuarioTimeRepository;

    private Time time;
    private Usuario admin;
    private Authentication currentAuth;

    @BeforeEach
    void setUp() {
        usuarioTimeRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();

        time = new Time(null, "Time para Postgres", BigDecimal.valueOf(50.00), true, null, null);
        time = timeRepository.save(time);

        admin = criarUsuarioPersistido("admin@email.com", "Admin do Time");
        vincular(admin, time, PerfilUsuario.ADMIN);

        autenticarComo(admin);
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

    private UsuarioTime vincular(Usuario usuario, Time time, PerfilUsuario perfil) {
        UsuarioTime vinculo = new UsuarioTime();
        vinculo.setUsuario(usuario);
        vinculo.setTime(time);
        vinculo.setPerfil(perfil);
        vinculo.setAtivo(true);
        return usuarioTimeRepository.save(vinculo);
    }

    private void autenticarComo(Usuario usuario) {
        currentAuth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
    }

    private RequestPostProcessor auth() {
        return request -> authentication(currentAuth).postProcessRequest(request);
    }

    @Test
    void addMembro_DeveCriarNovaIdentidade_QuandoEmailNaoExiste() throws Exception {
        AddMembroRequest request = new AddMembroRequest(
            "maria@email.com", "Maria Silva", "senha123", PerfilUsuario.ATLETA
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
            post("/api/time/{timeId}/membros", time.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.usuarioId", notNullValue()))
        .andExpect(jsonPath("$.nome", is("Maria Silva")))
        .andExpect(jsonPath("$.perfil", is("ATLETA")));

        assertThat(usuarioRepository.findByEmail("maria@email.com")).isPresent();
        assertThat(usuarioTimeRepository.findByTimeIdAndAtivoTrue(time.getId())).hasSize(2);
    }

    @Test
    void addMembro_DeveReaproveitarIdentidadeExistente_QuandoEmailJaCadastrado() throws Exception {
        Usuario existente = criarUsuarioPersistido("pessoa@email.com", "Pessoa Existente");

        AddMembroRequest request = new AddMembroRequest(
            "pessoa@email.com", null, null, PerfilUsuario.ATLETA
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(
            post("/api/time/{timeId}/membros", time.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isCreated())
        .andReturn();

        MembroResponse membro = objectMapper.readValue(result.getResponse().getContentAsString(), MembroResponse.class);

        assertThat(membro.usuarioId()).isEqualTo(existente.getId());
        // não cria uma segunda identidade com o mesmo email
        assertThat(usuarioRepository.findAll()).hasSize(2); // admin + existente
    }

    @Test
    void addMembro_DeveRetornarErro_QuandoJaEMembroAtivo() throws Exception {
        AddMembroRequest request = new AddMembroRequest(
            admin.getEmail(), null, null, PerfilUsuario.ATLETA
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
            post("/api/time/{timeId}/membros", time.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void findByTimeId_DeveRetornarApenasMembrosDoTime() throws Exception {
        Time outroTime = timeRepository.save(new Time(null, "Outro Time", BigDecimal.valueOf(75.00), true, null, null));
        Usuario outraPessoa = criarUsuarioPersistido("outra@email.com", "Outra Pessoa");
        vincular(outraPessoa, outroTime, PerfilUsuario.ADMIN);

        mockMvc.perform(
            get("/api/time/{timeId}/membros", time.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].usuarioId", is(admin.getId().toString())));
    }

    @Test
    void alterarPerfil_DeveRebaixarAtleta_QuandoNaoEUltimoAdmin() throws Exception {
        Usuario outroAdmin = criarUsuarioPersistido("outroadmin@email.com", "Outro Admin");
        UsuarioTime vinculoOutroAdmin = vincular(outroAdmin, time, PerfilUsuario.ADMIN);

        AlterarPerfilMembroRequest request = new AlterarPerfilMembroRequest(PerfilUsuario.ATLETA);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
            patch("/api/time/{timeId}/membros/{membroId}/perfil", time.getId(), vinculoOutroAdmin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.perfil", is("ATLETA")));
    }

    @Test
    void alterarPerfil_DeveRecusar_QuandoRebaixaUltimoAdmin() throws Exception {
        UsuarioTime vinculoAdmin = usuarioTimeRepository.findByUsuarioIdAndTimeId(admin.getId(), time.getId()).orElseThrow();

        AlterarPerfilMembroRequest request = new AlterarPerfilMembroRequest(PerfilUsuario.ATLETA);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
            patch("/api/time/{timeId}/membros/{membroId}/perfil", time.getId(), vinculoAdmin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(auth())
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void removerMembro_DeveRecusar_QuandoRemoveUltimoAdmin() throws Exception {
        UsuarioTime vinculoAdmin = usuarioTimeRepository.findByUsuarioIdAndTimeId(admin.getId(), time.getId()).orElseThrow();

        mockMvc.perform(
            delete("/api/time/{timeId}/membros/{membroId}", time.getId(), vinculoAdmin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void removerEReativarMembro_DeveFuncionar_QuandoNaoEUltimoAdmin() throws Exception {
        Usuario atleta = criarUsuarioPersistido("atleta@email.com", "Atleta");
        UsuarioTime vinculoAtleta = vincular(atleta, time, PerfilUsuario.ATLETA);

        mockMvc.perform(
            delete("/api/time/{timeId}/membros/{membroId}", time.getId(), vinculoAtleta.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isNoContent());

        assertThat(usuarioTimeRepository.findById(vinculoAtleta.getId()).orElseThrow().isAtivo()).isFalse();

        // A listagem precisa continuar trazendo o inativo — é dali que a tela oferece "reativar".
        mockMvc.perform(
            get("/api/time/{timeId}/membros", time.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.membroId=='" + vinculoAtleta.getId() + "')].ativo", contains(false)));

        mockMvc.perform(
            patch("/api/time/{timeId}/membros/{membroId}/reativar", time.getId(), vinculoAtleta.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(auth())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void multiTime_UsuarioDevePermanecerComIdentidadeUnica_AoEntrarEmDoisTimes() throws Exception {
        Time segundoTime = timeRepository.save(new Time(null, "Segundo Time", BigDecimal.valueOf(30.00), true, null, null));
        vincular(admin, segundoTime, PerfilUsuario.ATLETA);

        assertThat(usuarioRepository.findAll()).hasSize(1);
        assertThat(usuarioTimeRepository.findByUsuarioIdAndAtivoTrue(admin.getId())).hasSize(2);
    }
}
