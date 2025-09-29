package com.br.sistema.entities.Carro.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarroRequestDTO(

        @NotBlank(message = "Marca é obrigatória")
        String marca,

        @NotBlank(message = "Modelo é obrigatório")
        String modelo,

        @NotBlank(message = "Placa é obrigatória")
        @Size(min = 7, max = 7, message = "Placa deve ter 7 caracteres")
        String placa
) {}
