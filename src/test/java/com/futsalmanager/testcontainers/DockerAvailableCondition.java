package com.futsalmanager.testcontainers;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

/**
 * Condição customizada para verificar se Docker está disponível.
 * Se Docker não estiver disponível, os testes serão ignorados.
 */
public class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            // Tentar executar docker ps para verificar se Docker está disponível
            Process process = new ProcessBuilder("docker", "ps")
                .redirectErrorStream(true)
                .start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return ConditionEvaluationResult.enabled("Docker está disponível");
            } else {
                return ConditionEvaluationResult.disabled("Docker não está disponível ou não está rodando");
            }
        } catch (IOException | InterruptedException e) {
            return ConditionEvaluationResult.disabled("Docker não está instalado ou não é acessível: " + e.getMessage());
        }
    }
}
