package com.futsalmanager.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;

/**
 * Classe base para testes que usam TestContainers com PostgreSQL.
 * Automaticamente inicia um container PostgreSQL para os testes.
 *
 * NOTA: Requer Docker instalado e em execução.
 * Se Docker não estiver disponível, use application-test.yml com H2.
 */
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = AbstractTestcontainersTest.PostgresInitializer.class)
public abstract class AbstractTestcontainersTest {

    public static class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

        private static void startContainers(){
            Startables.deepStart(postgres).join();
        }

        private Map<String, Object> createConnectionConfiguration() {
            return Map.of(
                    "spring.datasource.url", postgres.getJdbcUrl(),
                    "spring.datasource.username", postgres.getUsername(),
                    "spring.datasource.password", postgres.getPassword()
            );
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            startContainers();
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            MapPropertySource propertySource = new MapPropertySource("postgres", createConnectionConfiguration());
            environment.getPropertySources().addFirst(propertySource);
        }
    }
}
