package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoResponseDTO(
        Long id,
        LocalDate dataSolicitacao,
        String status,
        String carroPlaca,
        String carroModelo,
        String motoristaNome,
        String usuarioNome,
        String setorNome,
        String destinoNome,
        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada
) {}
