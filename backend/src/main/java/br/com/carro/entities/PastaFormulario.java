package br.com.carro.entities;

import br.com.carro.entities.Usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_pasta_formulario")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PastaFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nome_pasta", nullable = false)
    private String nomePasta;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "caminho_completo", nullable = false)
    private String caminhoCompleto;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    private Usuario criadoPor;

    // Uma PastaFormulario pode ter vários Formulários
    @OneToMany(mappedBy = "pastaFormulario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Formulario> formularios;
}
