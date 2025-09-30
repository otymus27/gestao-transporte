package com.br.sistema.entities.Motorista.DTO;

public record SolicitacaoPorMotoristaDTO(
        Long motoristaId,
        String motoristaNome,
        Long total
) { }