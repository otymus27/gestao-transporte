package com.br.sistema.repositories;

import com.br.sistema.entities.FichaSolicitacao.FichaSolicitacao;
import com.br.sistema.entities.Usuario.DTO.ProducaoUsuarioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FichaSolicitacaoRepository extends JpaRepository<FichaSolicitacao, Long> {

    // Listagem paginada básica
    Page<FichaSolicitacao> findAll(Pageable pageable);

    // Busca por placa
    Page<FichaSolicitacao> findByPlacaVeiculoContainingIgnoreCase(String placa, Pageable pageable);

    // Busca por período
    Page<FichaSolicitacao> findByDataViagemBetween(LocalDate inicio, LocalDate fim, Pageable pageable);

    // Busca por usuário responsável — agora na ficha
    Page<FichaSolicitacao> findByUsuarioId(Long usuarioId, Pageable pageable);

    // Busca ficha com solicitações carregadas (evita N+1)
    @Query("SELECT DISTINCT f FROM FichaSolicitacao f " +
            "LEFT JOIN FETCH f.solicitacoes " +
            "WHERE f.id = :id")
    FichaSolicitacao findByIdComSolicitacoes(@Param("id") Long id);

    // Filtro dinâmico combinado
    // usuarioId agora filtra por f.usuario.id (usuario está na ficha)
    @Query("SELECT f FROM FichaSolicitacao f WHERE " +
            "(:placa IS NULL OR LOWER(f.placaVeiculo) LIKE LOWER(CONCAT('%', :placa, '%'))) AND " +
            "(:inicio IS NULL OR f.dataViagem >= :inicio) AND " +
            "(:fim IS NULL OR f.dataViagem <= :fim) AND " +
            "(:usuarioId IS NULL OR f.usuario.id = :usuarioId)")
    Page<FichaSolicitacao> filtrar(
            @Param("placa")     String placa,
            @Param("inicio")    LocalDate inicio,
            @Param("fim")       LocalDate fim,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );

    // =========================
    // PRODUÇÃO POR USUÁRIO (relatório)
    // Conta fichas e solicitações criadas por usuário no período,
    // com base em f.dataCriacao (quando o registro foi de fato digitado).
    // =========================
    @Query("SELECT new com.br.sistema.entities.Usuario.DTO.ProducaoUsuarioDTO(" +
            "u.id, u.nome, u.username, COUNT(DISTINCT f.id), COUNT(s.id)) " +
            "FROM FichaSolicitacao f " +
            "JOIN f.usuario u " +
            "LEFT JOIN f.solicitacoes s " +
            "WHERE (:nomeUsuario IS NULL OR :nomeUsuario = '' OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nomeUsuario, '%'))) AND " +
            "(:inicio IS NULL OR FUNCTION('date', f.dataCriacao) >= :inicio) AND " +
            "(:fim IS NULL OR FUNCTION('date', f.dataCriacao) <= :fim) " +
            "GROUP BY u.id, u.nome, u.username " +
            "ORDER BY u.nome")
    List<ProducaoUsuarioDTO> buscarProducaoPorUsuario(
            @Param("nomeUsuario") String nomeUsuario,
            @Param("inicio")      LocalDate inicio,
            @Param("fim")         LocalDate fim
    );
}