package com.br.sistema.entities.Motorista.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotoristaRequestDTO(

        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 15, message = "A matrícula deve ter no máximo 20 caracteres")
        String matricula,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,

        Boolean ativo
) { }
