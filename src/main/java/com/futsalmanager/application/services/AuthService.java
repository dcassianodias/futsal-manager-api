package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.LoginRequest;
import com.futsalmanager.api.dto.request.RegisterRequest;
import com.futsalmanager.api.dto.response.LoginResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TimeService timeService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository, TimeRepository timeRepository, PasswordEncoder passwordEncoder, TimeService timeService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.timeRepository = timeRepository;
        this.passwordEncoder = passwordEncoder;
        this.timeService = timeService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.senha()
                );

        var authentication = authenticationManager.authenticate(authToken);

        Usuario usuario = (Usuario) authentication.getPrincipal();

        /*
         * Como open-in-view está desabilitado, garantimos que
         * o relacionamento lazy com Time seja acessado dentro
         * de uma transação.
         */
        Usuario usuarioCompleto = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(usuarioCompleto);

        return criarLoginResponse(usuarioCompleto, token);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email já cadastrado");
        }

        /*
         * Reaproveita a criação do Time para manter a geração
         * do código centralizada no TimeService.
         */
        var timeResponse = timeService.create(
                new com.futsalmanager.api.dto.request.TimeCreateRequest(
                        request.nomeTime(),
                        request.valorMensalidade()
                )
        );

        Time time = timeRepository.findById(timeResponse.id())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Time não encontrado após criação"));

        Usuario usuario = new Usuario();

        usuario.setTime(time);
        usuario.setNome(request.nomeAdmin().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setGols(0);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuarioSalvo);

        return criarLoginResponse(usuarioSalvo, token);
    }

    private LoginResponse criarLoginResponse(
            Usuario usuario,
            String token) {

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiration(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.getTime().getId(),
                usuario.getTime().getNome()
        );
    }

}
