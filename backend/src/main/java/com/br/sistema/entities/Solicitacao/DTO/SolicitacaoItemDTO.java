package com.br.sistema.entities.Solicitacao.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record SolicitacaoItemDTO(

        @NotNull(message = "Motorista é obrigatório.")
        Long motoristaId,

        @NotNull(message = "Setor é obrigatório.")
        Long setorId,

        @NotNull(message = "Destino é obrigatório.")
        Long destinoId,

        // usuarioId REMOVIDO — usuário vem do token e fica na ficha

        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada,
        String status
) {}