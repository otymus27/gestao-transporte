package com.br.sistema.repositories;

import com.br.sistema.entities.Motorista.Motorista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    // Verifica se já existe motorista pela matrícula (para evitar duplicidade)
    boolean existsByMatricula(String matricula);

    @Query("""
           SELECT m FROM Motorista m
           WHERE (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
             AND (:matricula IS NULL OR LOWER(m.matricula) LIKE LOWER(CONCAT('%', :matricula, '%')))
           """)
    Page<Motorista> filtrar(@Param("nome") String nome,
                            @Param("matricula") String matricula,
                            Pageable pageable);

    // ✅ Para listagem (com paginação)
    @Query("""
           SELECT m FROM Motorista m
           WHERE (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
             AND (:matricula IS NULL OR LOWER(m.matricula) LIKE LOWER(CONCAT('%', :matricula, '%')))
           """)
    Page<Motorista> filtrarPaginado(@Param("nome") String nome,
                                    @Param("matricula") String matricula,
                                    Pageable pageable);

    // ✅ Para relatórios (sem paginação)
    @Query("""
           SELECT m FROM Motorista m
           WHERE (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
             AND (:matricula IS NULL OR LOWER(m.matricula) LIKE LOWER(CONCAT('%', :matricula, '%')))
           """)
    List<Motorista> filtrarRelatorio(@Param("nome") String nome,
                                     @Param("matricula") String matricula);

    Page<Motorista> findByMatriculaContainingIgnoreCaseOrNomeContainingIgnoreCase(
            String matricula,
            String nome,
            Pageable pageable
    );

}
