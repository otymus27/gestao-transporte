package com.br.sistema.entities.DTO;

import java.time.LocalDateTime;

public record PermissaoDTO(
        int status,
        String mensagem,
        String detalhe,
        String path,
        LocalDateTime timestamp
) {
}

