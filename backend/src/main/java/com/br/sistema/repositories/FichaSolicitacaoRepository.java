package com.br.sistema.repositories;

import com.br.sistema.entities.FichaSolicitacao.FichaSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FichaSolicitacaoRepository extends JpaRepository<FichaSolicitacao, Long> {

    // Listar todas com paginação
    Page<FichaSolicitacao> findAll(Pageable pageable);

    // Buscar por placa
    Page<FichaSolicitacao> findByPlacaVeiculoContainingIgnoreCase(String placa, Pageable pageable);

    // Buscar por período
    Page<FichaSolicitacao> findByDataViagemBetween(LocalDate inicio, LocalDate fim, Pageable pageable);

    // Buscar por usuário que criou
    Page<FichaSolicitacao> findByCriadoPorId(Long usuarioId, Pageable pageable);

    // Buscar ficha com todas as solicitações carregadas (evita N+1)
    @Query("SELECT DISTINCT f FROM FichaSolicitacao f " +
            "LEFT JOIN FETCH f.solicitacoes " +
            "WHERE f.id = :id")
    FichaSolicitacao findByIdComSolicitacoes(@Param("id") Long id);

    // Filtro dinâmico combinado
    @Query("SELECT f FROM FichaSolicitacao f WHERE " +
            "(:placa IS NULL OR LOWER(f.placaVeiculo) LIKE LOWER(CONCAT('%', :placa, '%'))) AND " +
            "(:inicio IS NULL OR f.dataViagem >= :inicio) AND " +
            "(:fim IS NULL OR f.dataViagem <= :fim) AND " +
            "(:usuarioId IS NULL OR f.criadoPor.id = :usuarioId)")
    Page<FichaSolicitacao> filtrar(
            @Param("placa") String placa,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );
}