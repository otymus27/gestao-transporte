package com.br.sistema.entities.Usuario.DTO;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.Usuario;

import java.util.List;

public record UsuarioDetalhadoDTO(
        Long id,
        String nome,
        String username,
        Boolean ativo,
        List<String> roles,
        List<SolicitacaoResumoDTO> solicitacoes
) {

    // ✅ Método de conversão com opção de incluir solicitações
    public static UsuarioDetalhadoDTO fromEntity(Usuario usuario, boolean incluirSolicitacoes) {
        List<SolicitacaoResumoDTO> solicitacoes = incluirSolicitacoes
                ? usuario.getSolicitacoes().stream()
                .map(UsuarioDetalhadoDTO::mapSolicitacaoResumo)
                .toList()
                : List.of();

        return new UsuarioDetalhadoDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.isAtivo(),
                usuario.getRoles().stream()
                        .map(role -> role.getNome())
                        .toList(),
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
