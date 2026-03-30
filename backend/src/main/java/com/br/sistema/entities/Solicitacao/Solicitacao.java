package com.br.sistema.entities.Solicitacao;

import com.br.sistema.entities.Carro.Carro;
import com.br.sistema.entities.Destino.Destino;
import com.br.sistema.entities.FichaSolicitacao.FichaSolicitacao;
import com.br.sistema.entities.Motorista.Motorista;
import com.br.sistema.entities.Setor.Setor;
import com.br.sistema.entities.Usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tb_solicitacao")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Solicitacao {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDate dataSolicitacao;

    @Column(nullable = false)
    private String status; // PENDENTE, APROVADA, RECUSADA

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_carro", nullable = false)
    private Carro carro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_motorista", nullable = false)
    private Motorista motorista;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_setor", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_destino", nullable = false)
    private Destino destino;

    @Column(name = "km_inicial")
    private Integer kmInicial;

    @Column(name = "km_final")
    private Integer kmFinal;

    @Column(name = "hora_saida")
    private LocalTime horaSaida;

    @Column(name = "hora_chegada")
    private LocalTime horaChegada;

    // Adicione este campo na Solicitacao existente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha")
    @JsonBackReference("ficha-solicitacao")
    private FichaSolicitacao ficha;

}