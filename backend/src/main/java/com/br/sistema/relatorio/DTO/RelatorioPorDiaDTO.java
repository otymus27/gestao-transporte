package com.br.sistema.relatorio.DTO;

import java.sql.Date;
import java.time.LocalDate;

public record RelatorioPorDiaDTO(
        Date data,
        Long quantidade
) {
    public LocalDate getDataAsLocalDate() {
        return data.toLocalDate();
    }
}
