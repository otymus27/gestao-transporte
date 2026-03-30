package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalTime;

public record SolicitacaoItemDTO(
        Long motoristaId,
        Long setorId,
        Long destinoId,
        Long usuarioId,
        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada,
        String status
) {}
