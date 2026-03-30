package com.br.sistema.entities.FichaSolicitacao.DTO;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoItemDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record FichaSolicitacaoRequestDTO(

        @NotNull(message = "Data da viagem é obrigatória.")
        LocalDate dataViagem,

        @NotBlank(message = "Placa do veículo é obrigatória.")
        String placaVeiculo,

        @NotEmpty(message = "A ficha deve ter ao menos uma solicitação.")
        @Valid
        List<SolicitacaoItemDTO> solicitacoes
) {}
