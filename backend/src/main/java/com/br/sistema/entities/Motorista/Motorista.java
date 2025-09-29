package com.br.sistema.entities.Motorista;

import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "tb_motorista")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Motorista {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String telefone;

    //Relacionamento com Solicitacao - um motorista pode estar vinculado a várias solicitações
    @OneToMany(mappedBy = "motorista")
    @JsonIgnore
    private List<Solicitacao> solicitacoes;
}