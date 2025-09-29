package com.br.sistema.entities.Setor;

import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "tb_setor")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Setor {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    //Relacionamento com Solicitação - um setor pode ter várias solicitações
    @OneToMany(mappedBy = "setor")
    @JsonIgnore
    private List<Solicitacao> solicitacoes;

}