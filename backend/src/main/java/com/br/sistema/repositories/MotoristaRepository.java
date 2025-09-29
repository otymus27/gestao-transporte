package com.br.sistema.repositories;

import com.br.sistema.entities.Motorista.Motorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    // Verifica se já existe motorista pela matrícula (para evitar duplicidade)
    boolean existsByMatricula(String matricula);
}
