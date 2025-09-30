package com.br.sistema.entities.Setor.DTO;

public record SolicitacaoPorSetorDTO(
        Long setorId,
        String setorNome,
        Long total
) { }