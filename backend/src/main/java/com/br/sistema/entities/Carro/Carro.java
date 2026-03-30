package com.br.sistema.entities.Carro;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "tb_carro")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Carro {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false, unique = true)
    private String placa;

    @Column(nullable = false)
    private String tipo;

    // @OneToMany REMOVIDO — Solicitacao não referencia mais Carro
    // A placa do veículo agora está em FichaSolicitacao.placaVeiculo
}