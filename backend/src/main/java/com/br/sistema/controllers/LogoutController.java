package com.br.sistema.controllers;


import com.br.sistema.autenticacao.SessionTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class LogoutController {

    private final SessionTracker sessionTracker;

    public LogoutController(SessionTracker sessionTracker) {
        this.sessionTracker = sessionTracker;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            sessionTracker.removerSessaoAtiva(username);
            return ResponseEntity.ok("Usuário " + username + " deslogado com sucesso.");
        }
        return ResponseEntity.badRequest().body("Nenhum usuário autenticado encontrado.");
    }
}
