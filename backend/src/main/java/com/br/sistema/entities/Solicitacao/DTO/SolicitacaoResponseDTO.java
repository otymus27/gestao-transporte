package com.br.sistema.entities.Solicitacao.DTO;

import com.br.sistema.entities.Solicitacao.Solicitacao;

import java.time.LocalTime;

public record SolicitacaoResponseDTO(
        Long id,
        String status,
        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada,
        Long motoristaId,
        String motoristaNome,
        Long setorId,
        String setorNome,
        Long destinoId,
        String destinoNome
        // usuario REMOVIDO — está na FichaSolicitacaoResponseDTO
) {
    public static SolicitacaoResponseDTO fromEntity(Solicitacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getStatus(),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada(),
                s.getMotorista().getId(),
                s.getMotorista().getNome(),
                s.getSetor().getId(),
                s.getSetor().getNome(),
                s.getDestino().getId(),
                s.getDestino().getNome()
        );
    }
}