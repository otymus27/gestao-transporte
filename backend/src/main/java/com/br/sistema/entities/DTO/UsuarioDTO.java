package com.br.sistema.entities.DTO;

import com.br.sistema.entities.Role.Role;
import com.br.sistema.entities.Usuario.Usuario;
import java.util.Set;
import java.util.stream.Collectors;

public record UsuarioDTO(
        Long id,
        String username,
        String nome,
        boolean ativo,
        Set<String> roles
) {
    public UsuarioDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.isAtivo(),
                usuario.getRoles().stream().map(Role::getNome).collect(Collectors.toSet())
        );
    }
}
