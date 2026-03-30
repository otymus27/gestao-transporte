package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoRequestDTO(
        Long idCarro,
        Long idMotorista,
        Long idSetor,
        Long idDestino,
        Integer kmInicial,
        Integer kmFinal,
        String horaSaida,    // "HH:mm"
        String horaChegada,  // "HH:mm"
        String status        // "PENDENTE" por default
) {}
