package com.futsalmanager.config;

import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("!prod")
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            TimeRepository timeRepository,
            UsuarioRepository usuarioRepository,
            UsuarioTimeRepository usuarioTimeRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (usuarioRepository.findByEmail("admin@futsal.com").isPresent()) {
                return;
            }

            Time time = Time.criar(
                    "Futsal Manager FC",
                    BigDecimal.valueOf(50.00),
                    true
            );

            time.setCodigo("FM-001");

            time = timeRepository.save(time);

            Usuario usuario = new Usuario();

            usuario.setNome("Administrador");
            usuario.setEmail("admin@futsal.com");
            usuario.setSenha(
                    passwordEncoder.encode("123456")
            );
            usuario.setAtivo(true);

            usuario = usuarioRepository.save(usuario);

            UsuarioTime vinculo = new UsuarioTime();
            vinculo.setUsuario(usuario);
            vinculo.setTime(time);
            vinculo.setPerfil(PerfilUsuario.ADMIN);
            vinculo.setAtivo(true);

            usuarioTimeRepository.save(vinculo);
        };
    }
}
