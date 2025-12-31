package com.br.sistema.entities.Motorista.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MotoristaRelatorioDTO{
    private Long id;
    private String matricula;
    private String nome;
    private String telefone;
    private Boolean ativo;
}