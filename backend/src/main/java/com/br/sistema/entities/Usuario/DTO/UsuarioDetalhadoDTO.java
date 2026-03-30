package com.br.sistema.entities.Usuario.DTO;

import com.br.sistema.entities.Usuario.Usuario;

import java.util.List;

public record UsuarioDetalhadoDTO(
        Long id,
        String nome,
        String username,
        Boolean ativo,
        List<String> roles
        // solicitacoes REMOVIDO — Usuario não referencia mais Solicitacao diretamente
) {
    public static UsuarioDetalhadoDTO fromEntity(Usuario usuario, boolean incluirSolicitacoes) {
        return new UsuarioDetalhadoDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.isAtivo(),
                usuario.getRoles().stream()
                        .map(role -> role.getNome())
                        .toList()
        );
    }
}