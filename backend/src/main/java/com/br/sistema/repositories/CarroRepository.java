package com.br.sistema.repositories;

import com.br.sistema.entities.Carro.Carro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarroRepository extends JpaRepository<Carro, Long> {

    // 🔍 Buscar carro pela placa
    Optional<Carro> findByPlaca(String placa);

    // ✅ Verificar se já existe carro com a mesma placa
    boolean existsByPlaca(String placa);
}
