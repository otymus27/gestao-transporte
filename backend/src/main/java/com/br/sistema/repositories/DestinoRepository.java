package com.br.sistema.repositories;

import com.br.sistema.entities.Destino.DTO.DestinoRelatorioDTO;
import com.br.sistema.entities.Destino.Destino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DestinoRepository extends JpaRepository<Destino, Long> {

    // Buscar por nome (útil para validações de duplicidade)
    Optional<Destino> findByNome(String nome);

    // ✅ Busca simples (autocomplete / pesquisa rápida)
    Page<Destino> findByNomeContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    @Query("""
        SELECT d FROM Destino d
        WHERE (:filtro IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    List<Destino> filtrarSemPaginacao(@Param("filtro") String filtro);

    // ✅ Filtro padrão (paginado)
    @Query("""
           SELECT d FROM Destino d
           WHERE (:nome IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
           """)
    Page<Destino> filtrar(
            @Param("nome") String nome,
            Pageable pageable
    );

    // ✅ Para listagem (com paginação)
    @Query("""
           SELECT d FROM Destino d
           WHERE (:nome IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
           """)
    Page<Destino> filtrarPaginado(
            @Param("nome") String nome,
            Pageable pageable
    );

    // ✅ Para relatórios (sem paginação)
    @Query("""
           SELECT d FROM Destino d
           WHERE (:nome IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
           """)
    List<Destino> filtrarRelatorio(
            @Param("nome") String nome
    );

    // ✅ Relatório completo (sem filtros)
    @Query("""
        SELECT new com.br.sistema.entities.Destino.DTO.DestinoRelatorioDTO(
            d.id,
            d.nome
        )
        FROM Destino d
        ORDER BY d.id, d.nome
    """)
    List<DestinoRelatorioDTO> listarParaRelatorio();

    // ✅ Relatório filtrado (sem paginação)
    @Query("""
        SELECT new com.br.sistema.entities.Destino.DTO.DestinoRelatorioDTO(
            d.id,
            d.nome
        )
        FROM Destino d
        WHERE (:nome IS NULL OR :nome = '' OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        ORDER BY d.id, d.nome
    """)
    List<DestinoRelatorioDTO> listarParaRelatorioFiltrado(
            @Param("nome") String nome
    );

    // ✅ Consulta de relatório (filtrado + paginado)
    @Query("""
        SELECT new com.br.sistema.entities.Destino.DTO.DestinoRelatorioDTO(
            d.id,
            d.nome
        )
        FROM Destino d
        WHERE (:nome IS NULL OR :nome = '' OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
    """)
    Page<DestinoRelatorioDTO> listarParaConsultaRelatorioPaginado(
            @Param("nome") String nome,
            Pageable pageable
    );
}
