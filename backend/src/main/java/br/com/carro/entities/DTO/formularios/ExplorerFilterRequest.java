package br.com.carro.entities.DTO.formularios;

import java.time.LocalDateTime;

public record ExplorerFilterRequest(
        // 🔹 filtros de pasta
        String nomePasta,
        Long tamanhoMin,
        Long tamanhoMax,

        // 🔹 filtros de arquivo
        String nomeArquivo,
        String extensao,
        LocalDateTime dataUploadInicial,
        LocalDateTime dataUploadFinal,

        // 🔹 ordenação
        String sortBy,
        String order
) {}
