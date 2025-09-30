package com.br.sistema.entities.Setor.DTO;

import jakarta.validation.constraints.NotBlank;

public record SetorRequestDTO(

        @NotBlank(message = "O nome do setor é obrigatório")
        String nome

) {}