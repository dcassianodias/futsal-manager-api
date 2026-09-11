package com.futsalmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origin:*}")
    private String allowedOrigin;

    /**
     * Exposto como CorsConfigurationSource (não CorsFilter solto) e ligado via
     * SecurityConfig.cors(...) — assim o Spring Security insere o tratamento de CORS
     * na frente da própria cadeia de filtros, garantindo que os cabeçalhos
     * Access-Control-Allow-* saiam até em respostas de erro (401/403) geradas pela
     * autenticação. Com um CorsFilter @Bean solto (sem ordem explícita), essas respostas
     * de erro são enviadas antes do filtro rodar, e o navegador reporta "bloqueado por CORS"
     * escondendo o erro real.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        for (String origin : allowedOrigin.split(",")) {
            config.addAllowedOriginPattern(origin.trim());
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
