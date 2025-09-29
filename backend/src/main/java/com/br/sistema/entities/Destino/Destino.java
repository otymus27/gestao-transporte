package com.br.sistema.entities.Destino;

import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "tb_destino")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Destino {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;


    //Relacionamento com Solicitação - um destino pode ter várias solicitações
    @OneToMany(mappedBy = "destino")
    @JsonIgnore
    private List<Solicitacao> solicitacoes;
}