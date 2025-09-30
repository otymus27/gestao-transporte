package com.br.sistema.entities.Setor.DTO;

import com.br.sistema.entities.Setor.Setor;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;

import java.util.List;

public record SetorDetalhadoDTO(
        Long id,
        String nome,
        List<SolicitacaoResumoDTO> solicitacoes
) {

    // ✅ Método de conversão com opção de incluir solicitações
    public static SetorDetalhadoDTO fromEntity(Setor setor, boolean incluirSolicitacoes) {
        List<SolicitacaoResumoDTO> solicitacoes = incluirSolicitacoes
                ? setor.getSolicitacoes().stream()
                .map(SetorDetalhadoDTO::mapSolicitacaoResumo)
                .toList()
                : List.of();

        return new SetorDetalhadoDTO(
                setor.getId(),
                setor.getNome(),
                solicitacoes
        );
    }

    // ✅ Método auxiliar interno para mapear Solicitação resumida
    private static SolicitacaoResumoDTO mapSolicitacaoResumo(Solicitacao s) {
        return new SolicitacaoResumoDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus()
        );
    }
}