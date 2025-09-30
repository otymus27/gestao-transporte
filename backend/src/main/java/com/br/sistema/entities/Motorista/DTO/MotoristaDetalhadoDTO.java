package com.br.sistema.entities.Motorista.DTO;

import com.br.sistema.entities.Motorista.Motorista;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;

import java.util.List;

public record MotoristaDetalhadoDTO(
        Long id,
        String matricula,
        String nome,
        String telefone,
        List<SolicitacaoResumoDTO> solicitacoes
) {

    // ✅ Método de conversão com opção de incluir solicitações
    public static MotoristaDetalhadoDTO fromEntity(Motorista motorista, boolean incluirSolicitacoes) {
        List<SolicitacaoResumoDTO> solicitacoes = incluirSolicitacoes
                ? motorista.getSolicitacoes().stream()
                .map(MotoristaDetalhadoDTO::mapSolicitacaoResumo)
                .toList()
                : List.of();

        return new MotoristaDetalhadoDTO(
                motorista.getId(),
                motorista.getMatricula(),
                motorista.getNome(),
                motorista.getTelefone(),
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
