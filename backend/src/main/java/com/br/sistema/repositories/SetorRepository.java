package com.br.sistema.repositories;

import com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO;
import com.br.sistema.entities.Setor.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    // Buscar por nome (útil para validações de duplicidade)
    Optional<Setor> findByNome(String nome);

    // ✅ Busca simples (autocomplete / pesquisa rápida)
    Page<Setor> findByNomeContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    @Query("""
        SELECT s FROM Setor s
        WHERE (:filtro IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    List<Setor> filtrarSemPaginacao(@Param("filtro") String filtro);

    // ✅ Filtro padrão (paginado)
    @Query("""
           SELECT s FROM Setor s
           WHERE (:nome IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
           """)
    Page<Setor> filtrar(
            @Param("nome") String nome,
            Pageable pageable
    );

    // ✅ Para listagem (com paginação)
    @Query("""
           SELECT s FROM Setor s
           WHERE (:nome IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
           """)
    Page<Setor> filtrarPaginado(
            @Param("nome") String nome,
            Pageable pageable
    );


    // ✅ Para relatórios (sem paginação)
    @Query("""
           SELECT s FROM Setor s
           WHERE (:nome IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
           """)
    List<Setor> filtrarRelatorio(
            @Param("nome") String nome
    );

    // ✅ Relatório completo (sem filtros)
    @Query("""
        SELECT new com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO(
            s.id,
            s.nome
        )
        FROM Setor s
        ORDER BY s.id, s.nome
    """)
    List<SetorRelatorioDTO> listarParaRelatorio();

    // ✅ Relatório filtrado (sem paginação)
    @Query("""
        SELECT new com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO(
            s.id,
            s.nome
        )
        FROM Setor s
        WHERE (:nome IS NULL OR :nome = '' OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        ORDER BY s.id, s.nome
    """)
    List<SetorRelatorioDTO> listarParaRelatorioFiltrado(
            @Param("nome") String nome
    );

    // ✅ Consulta de relatório (filtrado + paginado)
    @Query("""
        SELECT new com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO(
            s.id,
            s.nome
        )
        FROM Setor s
        WHERE (:nome IS NULL OR :nome = '' OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
    """)
    Page<SetorRelatorioDTO> listarParaConsultaRelatorioPaginado(
            @Param("nome") String nome,
            Pageable pageable
    );



}
