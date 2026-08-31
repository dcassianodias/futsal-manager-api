package com.futsalmanager.application.services;

import com.futsalmanager.api.dto.request.LoginRequest;
import com.futsalmanager.api.dto.request.RegisterRequest;
import com.futsalmanager.api.dto.response.LoginResponse;
import com.futsalmanager.api.dto.response.MembroTimeResponse;
import com.futsalmanager.application.exceptions.BusinessException;
import com.futsalmanager.application.exceptions.ResourceNotFoundException;
import com.futsalmanager.domain.entities.Time;
import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import com.futsalmanager.infrastructure.repositories.TimeRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioRepository;
import com.futsalmanager.infrastructure.repositories.UsuarioTimeRepository;
import com.futsalmanager.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioTimeRepository usuarioTimeRepository;
    private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TimeService timeService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       UsuarioRepository usuarioRepository, UsuarioTimeRepository usuarioTimeRepository,
                       TimeRepository timeRepository, PasswordEncoder passwordEncoder, TimeService timeService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioTimeRepository = usuarioTimeRepository;
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

        Usuario usuarioCompleto = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(usuarioCompleto);

        return criarLoginResponse(usuarioCompleto, token);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        Usuario usuarioSalvo = usuarioRepository.findByEmail(email)
                .map(existente -> vincularNovoTimeAIdentidadeExistente(existente, request))
                .orElseGet(() -> criarNovaIdentidadeComoAdmin(email, request));

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

        UsuarioTime vinculo = new UsuarioTime();
        vinculo.setUsuario(usuarioSalvo);
        vinculo.setTime(time);
        vinculo.setPerfil(PerfilUsuario.ADMIN);
        vinculo.setAtivo(true);
        usuarioTimeRepository.save(vinculo);

        String token = jwtService.generateToken(usuarioSalvo);

        return criarLoginResponse(usuarioSalvo, token);
    }

    /**
     * E-mail já existe: só deixa criar o novo time reaproveitando essa identidade
     * se a senha informada bater com a da conta existente — sem isso, qualquer um
     * poderia criar um time "em nome" de um e-mail alheio sem saber a senha.
     */
    private Usuario vincularNovoTimeAIdentidadeExistente(Usuario existente, RegisterRequest request) {
        if (!passwordEncoder.matches(request.senha(), existente.getSenha())) {
            throw new BusinessException("Este e-mail já tem uma conta. Informe a senha dessa conta para criar mais um time com ela, ou entre e adicione o time por lá.");
        }
        if (!existente.isAtivo()) {
            throw new BusinessException("Esta conta está desativada.");
        }
        return existente;
    }

    private Usuario criarNovaIdentidadeComoAdmin(String email, RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nomeAdmin().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setAtivo(true);
        usuario.setGols(0);
        return usuarioRepository.save(usuario);
    }

    private LoginResponse criarLoginResponse(
            Usuario usuario,
            String token) {

        List<MembroTimeResponse> times = usuarioTimeRepository.findByUsuarioIdAndAtivoTrue(usuario.getId())
                .stream()
                .map(vinculo -> new MembroTimeResponse(
                        vinculo.getTime().getId(),
                        vinculo.getTime().getNome(),
                        vinculo.getPerfil()
                ))
                .toList();

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiration(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                times
        );
    }

}
