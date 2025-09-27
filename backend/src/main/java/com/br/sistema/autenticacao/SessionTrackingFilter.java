package com.br.sistema.autenticacao;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionTrackingFilter extends OncePerRequestFilter {

    private final SessionTracker sessionTracker;

    public SessionTrackingFilter(SessionTracker sessionTracker) {
        this.sessionTracker = sessionTracker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                String username = null;

                // 🔑 Caso o principal seja um JWT (Spring Resource Server)
                if (principal instanceof Jwt jwt) {
                    username = jwt.getSubject();
                }
                // 🔑 Caso o principal seja UserDetails (ex: auth direta)
                else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    username = userDetails.getUsername();
                }

                if (username != null && !sessionTracker.isUsuarioAtivo(username)) {
                    sessionTracker.registrarLogin(username);
                }

            }
        } catch (Exception e) {
            logger.error("Erro ao rastrear sessão do usuário", e);
        }

        filterChain.doFilter(request, response);
    }
}
