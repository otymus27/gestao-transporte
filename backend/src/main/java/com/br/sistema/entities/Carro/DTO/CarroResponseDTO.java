package com.br.sistema.entities.Carro.DTO;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;

import java.util.List;

public record CarroResponseDTO(
        Long id,
        String marca,
        String modelo,
        String tipo,
        String placa,
        List<SolicitacaoResumoDTO> solicitacoes
) {
}