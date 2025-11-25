package com.br.sistema.services;


import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.UsuarioInativoException;
import com.br.sistema.repositories.UsuarioRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class AuthenticationService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public AuthenticationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado com o login: " + username));

        // 👉 Aqui entra a regra de inativo
        // Ajuste conforme o campo da sua entidade (ativo / status / enabled etc.)

        // Exemplo 1: campo boolean "ativo"
        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException("Sua conta está inativa. Entre em contato com o administrador.");
        }


        return usuario; // assumindo que Usuario implementa UserDetails

    }




}
