package com.br.sistema.repositories;

import com.br.sistema.entities.Destino.Destino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DestinoRepository extends JpaRepository<Destino, Long> {

    // Buscar por nome exato (útil para validação de duplicidade)
    Optional<Destino> findByNome(String nome);

    // Buscar por parte do nome (case insensitive) com paginação
    Page<Destino> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
