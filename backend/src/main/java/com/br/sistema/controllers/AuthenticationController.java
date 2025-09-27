package com.br.sistema.controllers;

import com.br.sistema.autenticacao.SessionTracker;
import com.br.sistema.entities.Login.LoginRequest;
import com.br.sistema.entities.Login.LoginResponse;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.UsuarioRepository;
import com.br.sistema.services.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final SessionTracker sessionTracker;

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService,UsuarioRepository usuarioRepository,  SessionTracker sessionTracker) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.sessionTracker = sessionTracker;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {

        // ✅ Coloque um breakpoint aqui
        System.out.println("Tentativa de login para usuário: " + loginRequest.username());


        // Autenticação
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);

        // Gera token
        String token = tokenService.gerarToken(authentication);


        // 1. Obtém o nome de usuário autenticado -  Busca UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 2. Busca o usuário do banco de dados para obter o campo senhaProvisoria
        // O orElseThrow lança uma exceção caso o usuário não seja encontrado (o que não deve acontecer após a autenticação)
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        // 3. Constrói a resposta com a informação da senha provisória
        boolean isSenhaProvisoria = usuario.isSenhaProvisoria();

        // 🔥 Registra no SessionTracker
        sessionTracker.registrarLogin(userDetails.getUsername());

        // 4. Retorna o token, o tempo de expiração e a senha provisoria conforme pede a assinatura do LoginResponse
        return new LoginResponse(token, 36000L, isSenhaProvisoria);
    }
}
