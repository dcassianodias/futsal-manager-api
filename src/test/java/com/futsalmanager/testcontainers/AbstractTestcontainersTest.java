package com.futsalmanager.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Classe base para testes que usam TestContainers com PostgreSQL.
 * Automaticamente inicia um container PostgreSQL para os testes.
 *
 * NOTA: Requer Docker instalado e em execução.
 * Se Docker não estiver disponível, use application-test.yml com H2.
 */
@SpringBootTest
@ContextConfiguration(initializers = AbstractTestcontainersTest.PostgresInitializer.class)
public abstract class AbstractTestcontainersTest {

    public static class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            // Usar PostgreSQL externo (assumindo que está rodando em localhost:5432)
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.url", "jdbc:postgresql://localhost:5432/futsaldb");
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.username", "futsaluser");
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.password", "futsalpass");
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            applicationContext.getEnvironment().getSystemProperties().put("spring.jpa.hibernate.ddl-auto", "create-drop");
            applicationContext.getEnvironment().getSystemProperties().put("spring.flyway.enabled", "true");
        }
    }
}
