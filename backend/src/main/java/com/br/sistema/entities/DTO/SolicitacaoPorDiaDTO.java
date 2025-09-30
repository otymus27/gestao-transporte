package com.br.sistema.entities.DTO;

import java.time.LocalDate;

/**
 * DTO unificado para consultas de Solicitação por dia
 * Usado tanto em Dashboard quanto em Solicitação.
 */
public record SolicitacaoPorDiaDTO(
        LocalDate data,
        Long total
) {
    // Construtor adicional para aceitar java.sql.Date (quando o Hibernate retornar)
    public SolicitacaoPorDiaDTO(java.sql.Date data, Long total) {
        this(data.toLocalDate(), total);
    }
}
