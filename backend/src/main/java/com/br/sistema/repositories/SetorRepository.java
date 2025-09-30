package com.br.sistema.repositories;

import com.br.sistema.entities.Setor.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    // Buscar por nome (útil para validações de duplicidade)
    Optional<Setor> findByNome(String nome);

    Page<Setor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

}
