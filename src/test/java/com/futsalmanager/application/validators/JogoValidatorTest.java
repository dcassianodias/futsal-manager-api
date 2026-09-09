package com.futsalmanager.application.validators;

import com.futsalmanager.api.dto.request.FinalizarJogoRequest;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.infrastructure.repositories.JogoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class JogoValidatorTest {

    @Mock
    private JogoRepository jogoRepository;

    private final JogoValidator validator = new JogoValidator(jogoRepository);

    @Test
    void deveRecusarQuandoNenhumGolMasArtilheirosInformadosNoPrimeiroQuadro() {
        FinalizarJogoRequest request = new FinalizarJogoRequest(0, 2, List.of(UUID.randomUUID()), 0, 0, null);

        assertThatThrownBy(() -> validator.validarArtilheiros(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveRecusarQuandoArtilheirosExcedemPlacarNoPrimeiroQuadro() {
        UUID usuarioId = UUID.randomUUID();
        FinalizarJogoRequest request = new FinalizarJogoRequest(1, 0, List.of(usuarioId, usuarioId, usuarioId), 0, 0, null);

        assertThatThrownBy(() -> validator.validarArtilheiros(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveRecusarQuandoArtilheirosFicamAbaixoDoPlacarNoPrimeiroQuadro() {
        FinalizarJogoRequest request = new FinalizarJogoRequest(2, 0, List.of(UUID.randomUUID()), 0, 0, null);

        assertThatThrownBy(() -> validator.validarArtilheiros(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void devePermitirQuandoContagemBateComPlacarNosDoisQuadros() {
        UUID usuarioId = UUID.randomUUID();
        FinalizarJogoRequest request = new FinalizarJogoRequest(
                2, 0, List.of(usuarioId, usuarioId),
                1, 0, List.of(usuarioId)
        );

        assertThatNoException().isThrownBy(() -> validator.validarArtilheiros(request));
    }

    @Test
    void devePermitirPlacarZeroSemArtilheirosNosDoisQuadros() {
        FinalizarJogoRequest request = new FinalizarJogoRequest(0, 0, null, 0, 0, null);

        assertThatNoException().isThrownBy(() -> validator.validarArtilheiros(request));
    }

    @Test
    void deveRecusarQuandoArtilheirosDoSegundoQuadroNaoBatemComPlacar() {
        FinalizarJogoRequest request = new FinalizarJogoRequest(1, 0, List.of(UUID.randomUUID()), 2, 1, List.of(UUID.randomUUID()));

        assertThatThrownBy(() -> validator.validarArtilheiros(request))
                .isInstanceOf(BusinessException.class);
    }
}
