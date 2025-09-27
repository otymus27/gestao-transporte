package com.br.sistema.entities.DTO;


import com.br.sistema.entities.Usuario.Usuario;

//Traz a lista de usuarios por pasta
public record UsuarioResumoDTO(
        Long id,
        String username,
        String nome

) {
    public static UsuarioResumoDTO fromEntity(Usuario usuario) {
        return new UsuarioResumoDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome()
        );
    }
}
