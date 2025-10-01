package com.br.sistema.repositories;

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

    Page<Setor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);


    @Query("""
        SELECT s FROM Setor s
        WHERE (:filtro IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    Page<Setor> filtrar(@Param("filtro") String filtro, Pageable pageable);

    @Query("""
        SELECT s FROM Setor s
        WHERE (:filtro IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    List<Setor> filtrarSemPaginacao(@Param("filtro") String filtro);

}
