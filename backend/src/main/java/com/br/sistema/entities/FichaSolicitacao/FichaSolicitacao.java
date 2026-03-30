package com.br.sistema.entities.FichaSolicitacao;

import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_ficha_solicitacao")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FichaSolicitacao {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_viagem", nullable = false)
    private LocalDate dataViagem;

    @Column(name = "placa_veiculo", nullable = false, length = 10)
    private String placaVeiculo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Usuário responsável pela ficha — vem do token (logado)
    // Movido da Solicitacao para cá: um usuário é dono da ficha inteira
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Solicitacao> solicitacoes = new ArrayList<>();
}