package com.br.sistema.entities.Destino.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO usado para criação/atualização de Destino
 */
public record DestinoRequestDTO(
        @NotBlank(message = "O nome do destino é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String nome
) {}
