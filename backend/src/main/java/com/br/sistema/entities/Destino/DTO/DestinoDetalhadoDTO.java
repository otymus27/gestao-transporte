package com.br.sistema.entities.Destino.DTO;

import com.br.sistema.entities.Destino.Destino;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;

import java.util.List;

public record DestinoDetalhadoDTO(
        Long id,
        String nome,
        List<SolicitacaoResumoDTO> solicitacoes
) {

    // ✅ Método de conversão com opção de incluir solicitações
    public static DestinoDetalhadoDTO fromEntity(Destino destino, boolean incluirSolicitacoes) {
        List<SolicitacaoResumoDTO> solicitacoes = incluirSolicitacoes
                ? destino.getSolicitacoes().stream()
                .map(DestinoDetalhadoDTO::mapSolicitacaoResumo)
                .toList()
                : List.of();

        return new DestinoDetalhadoDTO(
                destino.getId(),
                destino.getNome(),
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
