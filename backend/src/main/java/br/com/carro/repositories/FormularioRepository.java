package br.com.carro.repositories;

import br.com.carro.entities.Formulario;
import br.com.carro.entities.PastaFormulario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormularioRepository extends JpaRepository<Formulario, Long> {

    // Todos os formulários de uma pasta
    List<Formulario> findByPastaFormulario(PastaFormulario pastaFormulario);

    // Com paginação
    Page<Formulario> findByPastaFormulario(PastaFormulario pastaFormulario, Pageable pageable);

    // Contagem de formulários em uma pasta
    long countByPastaFormularioId(Long pastaId);
}