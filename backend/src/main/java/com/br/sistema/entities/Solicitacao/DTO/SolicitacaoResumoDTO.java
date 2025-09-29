package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitacaoResumoDTO(
        Long id,
        LocalDate dataSolicitacao,
        String status
) { }
