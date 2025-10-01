package com.br.sistema.entities.Solicitacao.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoRelatorioDTO(
        Long id,
        LocalDate dataSolicitacao,
        String status,
        String carro,
        String motorista,
        String usuario,
        String setor,
        String destino,
        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada
) {
}

