package com.br.sistema.entities.Carro;

import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

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

    //Relacionamento com Solicitacao - um carro pode estar vinculado a várias solicitações
    @OneToMany(mappedBy = "carro")
    @JsonBackReference("carro-solicitacoes")
    private List<Solicitacao> solicitacoes;
}