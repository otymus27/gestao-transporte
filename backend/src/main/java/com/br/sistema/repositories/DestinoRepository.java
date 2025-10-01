package com.br.sistema.repositories;

import com.br.sistema.entities.Destino.Destino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DestinoRepository extends JpaRepository<Destino, Long> {

    // Buscar por nome exato (útil para validação de duplicidade)
    Optional<Destino> findByNome(String nome);

    // Buscar por parte do nome (case insensitive) com paginação
    Page<Destino> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("""
        SELECT d FROM Destino d
        WHERE (:filtro IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    Page<Destino> filtrar(@Param("filtro") String filtro, Pageable pageable);

    @Query("""
        SELECT d FROM Destino d
        WHERE (:filtro IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    List<Destino> filtrarSemPaginacao(@Param("filtro") String filtro);
}
