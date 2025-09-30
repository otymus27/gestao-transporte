package com.br.sistema.entities.Usuario.DTO;

public record SolicitacaoPorUsuarioDTO(
        Long usuarioId,
        String usuarioNome,
        Long total
) { }
