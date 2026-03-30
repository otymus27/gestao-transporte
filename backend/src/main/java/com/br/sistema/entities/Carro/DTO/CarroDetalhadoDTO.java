package com.br.sistema.entities.Carro.DTO;

import com.br.sistema.entities.Carro.Carro;

public record CarroDetalhadoDTO(
        Long id,
        String placa,
        String marca,
        String modelo,
        String tipo
        // solicitacoes REMOVIDO — Carro não referencia mais Solicitacao
) {
    public static CarroDetalhadoDTO fromEntity(Carro carro, boolean incluirSolicitacoes) {
        return new CarroDetalhadoDTO(
                carro.getId(),
                carro.getPlaca(),
                carro.getMarca(),
                carro.getModelo(),
                carro.getTipo()
        );
    }
}