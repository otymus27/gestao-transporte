package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoRequestDTO(
        LocalDate dataSolicitacao,
        String status,
        Long carroId,
        Long motoristaId,
        Long usuarioId,
        Long setorId,
        Long destinoId,
        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada
) {}
