package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.PagamentoCreateRequest;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.domain.enums.TipoPagamento;
import com.futsalmanager.infrastructure.repositories.PagamentoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração com PostgreSQL via TestContainers provando, com autenticação
 * real de ponta a ponta, que pagamento de uma pessoa é privado: atleta só pode ver
 * o próprio, admin vê de todo mundo.
 *
 * NOTA: Requer Docker instalado e em execução.
 */
@AutoConfigureMockMvc
@Transactional
@ExtendWith(DockerAvailableCondition.class)
class PagamentoControllerPostgresIntegrationTest extends AbstractTestcontainersTest {

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

    @Autowired
    private PagamentoRepository pagamentoRepository;

    private Time time;
    private Usuario admin;
    private Usuario atleta;
    private Authentication currentAuth;

    @BeforeEach
    void setUp() throws Exception {
        pagamentoRepository.deleteAll();
        usuarioTimeRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();

        time = timeRepository.save(new Time(null, "Time Privacidade", BigDecimal.valueOf(50.00), true, null, null));

        admin = criarUsuarioPersistido("admin@email.com", "Admin");
        vincular(admin, PerfilUsuario.ADMIN);

        atleta = criarUsuarioPersistido("atleta@email.com", "Atleta");
        vincular(atleta, PerfilUsuario.ATLETA);

        autenticarComo(admin);
        criarPagamento(atleta.getId());
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

    private void vincular(Usuario usuario, PerfilUsuario perfil) {
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

    private RequestPostProcessor auth() {
        return request -> authentication(currentAuth).postProcessRequest(request);
    }

    private void criarPagamento(java.util.UUID usuarioId) throws Exception {
        PagamentoCreateRequest request = new PagamentoCreateRequest(
            time.getId(), usuarioId, null, LocalDate.of(2026, 8, 1), BigDecimal.valueOf(50.00), TipoPagamento.MENSALIDADE
        );
        mockMvc.perform(
            post("/api/pagamento/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth())
        ).andExpect(status().isCreated());
    }

    @Test
    void findByTime_AdminVeTodoMundo() throws Exception {
        autenticarComo(admin);
        mockMvc.perform(get("/api/pagamento/v1/time/{timeId}", time.getId()).with(auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void findByTime_AtletaTomaProibido_NaoPodeVerPagamentoDeTodoMundo() throws Exception {
        autenticarComo(atleta);
        mockMvc.perform(get("/api/pagamento/v1/time/{timeId}", time.getId()).with(auth()))
            .andExpect(status().isForbidden());
    }

    @Test
    void findPendentesByTime_AtletaTomaProibido() throws Exception {
        autenticarComo(atleta);
        mockMvc.perform(get("/api/pagamento/v1/time/{timeId}/pendentes", time.getId()).with(auth()))
            .andExpect(status().isForbidden());
    }

    @Test
    void findByUsuario_AtletaVeOProprio() throws Exception {
        autenticarComo(atleta);
        mockMvc.perform(get("/api/pagamento/v1/usuario/{usuarioId}", atleta.getId()).with(auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void findByUsuario_AtletaTomaProibido_QuandoConsultaOutraPessoa() throws Exception {
        Usuario outroAtleta = criarUsuarioPersistido("outro@email.com", "Outro Atleta");
        vincular(outroAtleta, PerfilUsuario.ATLETA);

        autenticarComo(atleta);
        mockMvc.perform(get("/api/pagamento/v1/usuario/{usuarioId}", outroAtleta.getId()).with(auth()))
            .andExpect(status().isForbidden());
    }
}
