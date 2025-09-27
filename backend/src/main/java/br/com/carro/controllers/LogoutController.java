package br.com.carro.controllers;

import br.com.carro.autenticacao.SessionTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
