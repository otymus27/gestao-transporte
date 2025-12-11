package com.br.sistema.relatorio.DTO;

import java.util.List;

// Envelope paginado simples (pra não depender de Page no JSON)
public record PaginadoDTO<T>(List<T> content, int page, int size, long totalElements) {}