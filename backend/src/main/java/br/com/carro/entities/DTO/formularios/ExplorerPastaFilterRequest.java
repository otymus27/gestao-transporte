package br.com.carro.entities.DTO.formularios;

public record ExplorerPastaFilterRequest(
        String nomePasta,
        Long tamanhoMin,
        Long tamanhoMax,
        String sortBy,
        String order
) {}

