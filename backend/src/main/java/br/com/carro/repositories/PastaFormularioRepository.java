package br.com.carro.repositories;

import br.com.carro.entities.PastaFormulario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PastaFormularioRepository extends JpaRepository<PastaFormulario, Long> {

    // Lista todas as pastas de formulários ordenadas por nome
    List<PastaFormulario> findAllByOrderByNomePastaAsc();
}
