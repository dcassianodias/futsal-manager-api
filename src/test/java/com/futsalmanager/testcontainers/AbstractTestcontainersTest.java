package com.futsalmanager.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.12")
            .withDatabaseName("futsaldb")
            .withUsername("futsaluser")
            .withPassword("futsalpass");

    public static class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            postgres.start();
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.url", postgres.getJdbcUrl());
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.username", postgres.getUsername());
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.password", postgres.getPassword());
            applicationContext.getEnvironment().getSystemProperties().put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            applicationContext.getEnvironment().getSystemProperties().put("spring.jpa.hibernate.ddl-auto", "create-drop");
            applicationContext.getEnvironment().getSystemProperties().put("spring.flyway.enabled", "true");
        }
    }
}
