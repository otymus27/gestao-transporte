package br.com.carro.entities.DTO.formularios;

import java.util.List;

public record PaginaDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}