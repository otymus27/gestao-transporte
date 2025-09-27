package br.com.carro.repositories;

import br.com.carro.entities.Arquivo;
import br.com.carro.entities.Pasta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Interface de repositório para a entidade Arquivo.
 * Estende JpaRepository para operações básicas de CRUD e paginação.
 * <p>
 * Estende JpaSpecificationExecutor para permitir consultas dinâmicas
 * usando a API de Criteria do JPA (o que resolve o erro de `findAll` com Specification).
 */
public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {


    List<Arquivo> findByPastaId(Long pastaId);

    // Busca todos os arquivos de uma pasta
    List<Arquivo> findByPasta(Pasta pasta);

    @Query("SELECT a FROM Arquivo a WHERE a.pasta = :pasta AND LOWER(a.nomeArquivo) LIKE LOWER(CONCAT('%', :nomeFiltro, '%'))")
    List<Arquivo> findByPastaAndNomeContainingIgnoreCase(@Param("pasta") Pasta pasta,
                                                         @Param("nomeFiltro") String nomeFiltro);

    @Query("SELECT a FROM Arquivo a WHERE a.pasta = :pasta AND LOWER(a.tipoMime) = LOWER(:extensaoFiltro)")
    List<Arquivo> findByPastaAndExtensaoIgnoreCase(@Param("pasta") Pasta pasta,
                                                   @Param("extensaoFiltro") String extensaoFiltro);

    @Query("SELECT a FROM Arquivo a WHERE a.pasta = :pasta AND LOWER(a.nomeArquivo) LIKE LOWER(CONCAT('%', :nomeFiltro, '%')) AND LOWER(a.tipoMime) = LOWER(:extensaoFiltro)")
    List<Arquivo> findByPastaAndNomeAndExtensao(@Param("pasta") Pasta pasta,
                                                @Param("nomeFiltro") String nomeFiltro,
                                                @Param("extensaoFiltro") String extensaoFiltro);



// -------USADOS PARA OS FORMULARIOS
    @Query("select coalesce(sum(a.tamanho), 0) from Arquivo a where a.pasta.id = :pastaId")
    Long sumBytesByPastaId(@Param("pastaId") Long pastaId);

    @Query("""
           select a from Arquivo a
           where a.pasta.id = :pastaId
             and (:extensao is null or :extensao = '' or lower(a.nomeArquivo) like concat('%.', lower(:extensao)))
           """)
    Page<Arquivo> findByPastaIdAndExtensao(@Param("pastaId") Long pastaId,
                                           @Param("extensao") String extensao,
                                           Pageable pageable);

    long countByPastaId(Long id);
}

