package com.futsalmanager.api.controller;

import com.futsalmanager.domain.entities.GolRegistro;
import com.futsalmanager.domain.entities.Jogo;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.ResultadoJogo;
import com.futsalmanager.domain.enums.StatusJogo;
import com.futsalmanager.infrastructure.repositories.GolRegistroRepository;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.testcontainers.AbstractTestcontainersTest;
import com.futsalmanager.testcontainers.DockerAvailableCondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração com PostgreSQL via TestContainers para /api/feed/publico.
 * NOTA: Requer Docker instalado e em execução.
 */
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeedPublicoControllerPostgresIntegrationTest extends AbstractTestcontainersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private GolRegistroRepository golRegistroRepository;

    @BeforeEach
    void setUp() {
        limparBase();
    }

    @Test
    void buscar_ComTimePublicoEJogoFinalizado_DeveRetornarJogoEArtilheiro() throws Exception {
        Time time = criarTime("Bola Murcha FC", true);
        Usuario usuario = criarUsuario("Rafael Souza");

        Jogo jogo = criarJogoFinalizado(time, "Real Quintal", LocalDateTime.now().minusHours(3), 4, 2);
        golRegistroRepository.save(new GolRegistro(jogo, usuario, 2));

        mockMvc.perform(get("/api/feed/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jogos[0].timeNome").value("Bola Murcha FC"))
                .andExpect(jsonPath("$.jogos[0].adversario").value("Real Quintal"))
                .andExpect(jsonPath("$.jogos[0].golsTime").value(4))
                .andExpect(jsonPath("$.jogos[0].golsAdversario").value(2))
                .andExpect(jsonPath("$.jogos[0].destaque").value("Rafael Souza — 2 gols"))
                .andExpect(jsonPath("$.jogos[0].aproveitamentoTime").value(100))
                .andExpect(jsonPath("$.artilheiros[0].nome").value("Rafael Souza"))
                .andExpect(jsonPath("$.artilheiros[0].timeNome").value("Bola Murcha FC"))
                .andExpect(jsonPath("$.artilheiros[0].gols").value(2));
    }

    @Test
    void buscar_ComTimePrivado_NaoDeveAparecerNoFeed() throws Exception {
        Time time = criarTime("Time Privado FC", false);
        criarJogoFinalizado(time, "Adversario Qualquer", LocalDateTime.now().minusHours(1), 3, 1);

        mockMvc.perform(get("/api/feed/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jogos").isEmpty())
                .andExpect(jsonPath("$.artilheiros").isEmpty());
    }

    private Time criarTime(String nome, boolean publico) {
        Time time = Time.criar(nome, BigDecimal.TEN, true);
        time.setPublico(publico);
        time.setCodigo("COD-" + UUID.randomUUID());
        return timeRepository.save(time);
    }

    private Usuario criarUsuario(String nome) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(nome.toLowerCase().replace(" ", ".") + "-" + UUID.randomUUID() + "@email.com");
        usuario.setSenha("senha-hash");
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    private Jogo criarJogoFinalizado(Time time, String adversario, LocalDateTime dataHora, int golsTime, int golsAdversario) {
        Jogo jogo = new Jogo(null, time, adversario, "Quadra Municipal", dataHora, StatusJogo.FINALIZADO, null, null, null);
        jogo.setGolsTime(golsTime);
        jogo.setGolsAdversario(golsAdversario);
        jogo.setResultado(golsTime > golsAdversario ? ResultadoJogo.VITORIA
                : golsTime < golsAdversario ? ResultadoJogo.DERROTA : ResultadoJogo.EMPATE);
        return jogoRepository.save(jogo);
    }

    @AfterEach
    void tearDown() {
        limparBase();
    }

    private void limparBase() {
        golRegistroRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();
        timeRepository.deleteAll();
    }
}
