package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoResponseDTO(
        Long id,
        LocalDate dataSolicitacao,
        String status,

        // Carro
        Long carroId,
        String carroPlaca,
        String carroModelo,

        // Motorista
        Long motoristaId,
        String motoristaNome,

        // Usuário
        Long usuarioId,
        String usuarioNome,   // ✅ exibição
        String username,  // ✅ identificador do usuário

        // Setor
        Long setorId,
        String setorNome,

        // Destino
        Long destinoId,
        String destinoNome,

        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada
) {}
