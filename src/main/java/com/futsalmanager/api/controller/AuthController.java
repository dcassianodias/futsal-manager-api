package com.futsalmanager.api.controller;

import com.futsalmanager.api.dto.request.EsqueciSenhaRequest;
import com.futsalmanager.api.dto.request.LoginRequest;
import com.futsalmanager.api.dto.request.RedefinirSenhaRequest;
import com.futsalmanager.api.dto.request.RegisterRequest;
import com.futsalmanager.api.dto.response.LoginResponse;
import com.futsalmanager.api.dto.response.MensagemResponse;
import com.futsalmanager.application.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        return ResponseEntity.ok(authService.esqueciSenha(request));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<MensagemResponse> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        return ResponseEntity.ok(authService.redefinirSenha(request));
    }
}
