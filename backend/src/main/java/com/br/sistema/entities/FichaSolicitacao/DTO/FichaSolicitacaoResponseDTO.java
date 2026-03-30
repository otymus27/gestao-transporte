package com.br.sistema.entities.FichaSolicitacao.DTO;

import com.br.sistema.entities.FichaSolicitacao.FichaSolicitacao;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FichaSolicitacaoResponseDTO(
        Long id,
        LocalDate dataViagem,
        String placaVeiculo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        // Usuário responsável pela ficha inteira
        Long usuarioId,
        String usuarioNome,
        String usuarioUsername,
        List<SolicitacaoResponseDTO> solicitacoes
) {
    public static FichaSolicitacaoResponseDTO fromEntity(FichaSolicitacao f) {
        return new FichaSolicitacaoResponseDTO(
                f.getId(),
                f.getDataViagem(),
                f.getPlacaVeiculo(),
                f.getDataCriacao(),
                f.getDataAtualizacao(),
                f.getUsuario() != null ? f.getUsuario().getId() : null,
                f.getUsuario() != null ? f.getUsuario().getNome() : null,
                f.getUsuario() != null ? f.getUsuario().getUsername() : null,
                f.getSolicitacoes().stream()
                        .map(SolicitacaoResponseDTO::fromEntity)
                        .toList()
        );
    }
}