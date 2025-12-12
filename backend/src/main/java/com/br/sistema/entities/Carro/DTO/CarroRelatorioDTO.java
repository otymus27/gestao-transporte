package com.br.sistema.entities.Carro.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CarroRelatorioDTO {
    private String placa;
    private String marca;
    private String modelo;
    private String tipo;
}