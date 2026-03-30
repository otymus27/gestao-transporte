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
        Long criadoPorId,
        String criadoPorNome,
        List<SolicitacaoResponseDTO> solicitacoes
) {
    public static FichaSolicitacaoResponseDTO fromEntity(FichaSolicitacao f) {
        return new FichaSolicitacaoResponseDTO(
                f.getId(),
                f.getDataViagem(),
                f.getPlacaVeiculo(),
                f.getDataCriacao(),
                f.getCriadoPor() != null ? f.getCriadoPor().getId() : null,
                f.getCriadoPor() != null ? f.getCriadoPor().getNome() : null,
                f.getSolicitacoes().stream()
                        .map(SolicitacaoResponseDTO::fromEntity)
                        .toList()
        );
    }
}