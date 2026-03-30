package com.br.sistema.entities.Solicitacao;

import com.br.sistema.entities.Destino.Destino;
import com.br.sistema.entities.FichaSolicitacao.FichaSolicitacao;
import com.br.sistema.entities.Motorista.Motorista;
import com.br.sistema.entities.Setor.Setor;
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

    // Espelho da data_viagem da ficha — facilita consultas e relatórios
    @Column(name = "data_solicitacao", nullable = false)
    private LocalDate dataSolicitacao;

    @Column(nullable = false, length = 20)
    private String status = "PENDENTE";

    @Column(name = "km_inicial")
    private Integer kmInicial;

    @Column(name = "km_final")
    private Integer kmFinal;

    @Column(name = "hora_saida")
    private LocalTime horaSaida;

    @Column(name = "hora_chegada")
    private LocalTime horaChegada;

    // id_usuario REMOVIDO — usuário é responsável pela ficha, não pela solicitação
    // id_carro   REMOVIDO — placa está na ficha

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_motorista", nullable = false)
    private Motorista motorista;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_setor", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_destino", nullable = false)
    private Destino destino;

    // Vínculo com a ficha master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha", nullable = false)
    @JsonBackReference
    private FichaSolicitacao ficha;
}