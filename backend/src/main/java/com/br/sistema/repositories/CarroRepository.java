package com.br.sistema.repositories;

import com.br.sistema.entities.Carro.Carro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarroRepository extends JpaRepository<Carro, Long> {

    // 🔍 Buscar carro pela placa
    Optional<Carro> findByPlaca(String placa);

    // ✅ Verificar se já existe carro com a mesma placa
    boolean existsByPlaca(String placa);

    @Query("""
           SELECT c FROM Carro c
           WHERE (:placa IS NULL OR LOWER(c.placa) LIKE LOWER(CONCAT('%', :placa, '%')))
             AND (:marca IS NULL OR LOWER(c.marca) LIKE LOWER(CONCAT('%', :marca, '%')))
             AND (:modelo IS NULL OR LOWER(c.modelo) LIKE LOWER(CONCAT('%', :modelo, '%')))
           """)
    Page<Carro> filtrar(@Param("placa") String placa,
                        @Param("marca") String marca,
                        @Param("modelo") String modelo,
                        Pageable pageable);

    @Query("""
        SELECT c FROM Carro c
        WHERE (:filtro IS NULL OR LOWER(c.marca) LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(c.modelo) LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(c.placa) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    Page<Carro> filtrar(String filtro, Pageable pageable);

    @Query("""
        SELECT c FROM Carro c
        WHERE (:filtro IS NULL OR LOWER(c.marca) LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(c.modelo) LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(c.placa) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    List<Carro> filtrarSemPaginacao(String filtro);




}
