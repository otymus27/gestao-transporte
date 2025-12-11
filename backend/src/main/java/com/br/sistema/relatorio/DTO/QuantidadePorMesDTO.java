package com.br.sistema.relatorio.DTO;

// Série mensal (ano + mês)
public record QuantidadePorMesDTO(Integer ano, Integer mes, Long quantidade) {
    public String labelAnoMes() { // ex: 2025-09
        return String.format("%04d-%02d", ano, mes);
    }
}
