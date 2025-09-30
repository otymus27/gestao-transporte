package com.br.sistema.entities.Carro.DTO;

import com.br.sistema.entities.Carro.Carro;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;

import java.util.List;

public record CarroDetalhadoDTO(
        Long id,
        String marca,
        String modelo,
        String placa,
        List<SolicitacaoResumoDTO> solicitacoes
) {

    // ✅ Método de conversão com opção de incluir solicitações
    public static CarroDetalhadoDTO fromEntity(Carro carro, boolean incluirSolicitacoes) {
        List<SolicitacaoResumoDTO> solicitacoes = incluirSolicitacoes
                ? carro.getSolicitacoes().stream()
                .map(CarroDetalhadoDTO::mapSolicitacaoResumo)
                .toList()
                : List.of();

        return new CarroDetalhadoDTO(
                carro.getId(),
                carro.getMarca(),
                carro.getModelo(),
                carro.getPlaca(),
                solicitacoes
        );
    }

    // ✅ Método auxiliar interno para mapear Solicitação
    private static SolicitacaoResumoDTO mapSolicitacaoResumo(Solicitacao s) {
        return new SolicitacaoResumoDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus()
        );
    }
}
