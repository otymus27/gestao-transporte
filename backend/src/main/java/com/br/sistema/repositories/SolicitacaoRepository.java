package com.br.sistema.repositories;

import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;
import com.br.sistema.entities.Dashboard.DTO.RankingItemDTO;
import com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO;
import com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoPorStatusDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO;
import com.br.sistema.relatorio.DTO.QuantidadePorMesDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    // =========================
    // CONSULTAS BÁSICAS
    // =========================

    Page<Solicitacao> findByStatus(String status, Pageable pageable);
    Page<Solicitacao> findBySetor_Id(Long setorId, Pageable pageable);
    Page<Solicitacao> findByMotorista_Id(Long motoristaId, Pageable pageable);
    Page<Solicitacao> findByDestino_Id(Long destinoId, Pageable pageable);
    Page<Solicitacao> findById(Long id, Pageable pageable);
    Page<Solicitacao> findByDataSolicitacaoBetween(LocalDate inicio, LocalDate fim, Pageable pageable);
    long countByStatus(String status);

    // Busca por usuário agora é via ficha
    @Query("SELECT s FROM Solicitacao s WHERE s.ficha.usuario.id = :usuarioId")
    Page<Solicitacao> findByFichaUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // =========================
    // DASHBOARD / AGRUPAMENTOS
    // =========================

    @Query("""
        SELECT new com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO(
            s.dataSolicitacao, COUNT(s)
        )
        FROM Solicitacao s
        WHERE s.dataSolicitacao BETWEEN :inicio AND :fim
        GROUP BY s.dataSolicitacao
        ORDER BY s.dataSolicitacao
    """)
    List<SolicitacaoPorDiaDTO> countByDia(@Param("inicio") LocalDate inicio,
                                          @Param("fim") LocalDate fim);

    @Query("""
        SELECT new com.br.sistema.entities.Dashboard.DTO.RankingItemDTO(
            se.nome, COUNT(s)
        )
        FROM Solicitacao s JOIN s.setor se
        GROUP BY se.nome
        ORDER BY COUNT(s) DESC
    """)
    List<RankingItemDTO> topSetores();

    @Query("""
        SELECT new com.br.sistema.entities.Dashboard.DTO.RankingItemDTO(
            m.nome, COUNT(s)
        )
        FROM Solicitacao s JOIN s.motorista m
        GROUP BY m.nome
        ORDER BY COUNT(s) DESC
    """)
    List<RankingItemDTO> topMotoristas();

    @Query("""
        SELECT new com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO(
            m.id, m.nome, COUNT(s)
        )
        FROM Solicitacao s JOIN s.motorista m
        GROUP BY m.id, m.nome
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorMotoristaDTO> buscarPorMotorista();

    @Query("""
        SELECT new com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO(
            se.id, se.nome, COUNT(s)
        )
        FROM Solicitacao s JOIN s.setor se
        GROUP BY se.id, se.nome
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorSetorDTO> buscarPorSetor();

    // Agrupamento por usuário agora é via ficha
    @Query("""
        SELECT new com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO(
            u.id, u.nome, COUNT(s)
        )
        FROM Solicitacao s JOIN s.ficha f JOIN f.usuario u
        GROUP BY u.id, u.nome
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorUsuarioDTO> buscarPorUsuario();

    @Query("""
        SELECT new com.br.sistema.entities.Solicitacao.DTO.SolicitacaoPorStatusDTO(
            s.status, COUNT(s)
        )
        FROM Solicitacao s
        GROUP BY s.status
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorStatusDTO> buscarPorStatus();

    @Query("""
        SELECT new com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO(
            DATE(s.dataSolicitacao), COUNT(s)
        )
        FROM Solicitacao s
        WHERE DATE(s.dataSolicitacao) BETWEEN :inicio AND :fim
        GROUP BY DATE(s.dataSolicitacao)
        ORDER BY DATE(s.dataSolicitacao)
    """)
    List<SolicitacaoPorDiaDTO> buscarPorDatas(@Param("inicio") LocalDate inicio,
                                              @Param("fim") LocalDate fim);

    // =========================
    // RELATÓRIO SEM PAGINAÇÃO
    // usuario agora via s.ficha.usuario
    // =========================

    @Query("""
        SELECT DISTINCT s
        FROM Solicitacao s
        JOIN FETCH s.motorista m
        JOIN FETCH s.setor se
        JOIN FETCH s.destino d
        JOIN FETCH s.ficha f
        JOIN FETCH f.usuario u
        WHERE (:filtro IS NULL OR :filtro = '' OR
                LOWER(m.nome)          LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(u.nome)          LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(se.nome)         LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(d.nome)          LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(s.status)        LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(f.placaVeiculo)  LIKE LOWER(CONCAT('%', :filtro, '%'))
        )
        ORDER BY s.id DESC
    """)
    List<Solicitacao> filtrarSemPaginacao(@Param("filtro") String filtro);

    // =========================
    // SÉRIE MENSAL
    // =========================

    @Query("""
        SELECT new com.br.sistema.relatorio.DTO.QuantidadePorMesDTO(
            YEAR(s.dataSolicitacao), MONTH(s.dataSolicitacao), COUNT(s)
        )
        FROM Solicitacao s
        WHERE FUNCTION('date', s.dataSolicitacao) BETWEEN :inicio AND :fim
        GROUP BY YEAR(s.dataSolicitacao), MONTH(s.dataSolicitacao)
        ORDER BY YEAR(s.dataSolicitacao), MONTH(s.dataSolicitacao)
    """)
    List<QuantidadePorMesDTO> contarPorMes(@Param("inicio") LocalDate inicio,
                                           @Param("fim") LocalDate fim);

    // =========================
    // FILTRO DINÂMICO
    // username/usuarioId REMOVIDOS — filtrar por usuário é via ficha
    // =========================

    @Query("SELECT s FROM Solicitacao s WHERE " +
            "(:id IS NULL OR s.id = :id) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:motoristaId IS NULL OR s.motorista.id = :motoristaId) AND " +
            "(:setorId IS NULL OR s.setor.id = :setorId) AND " +
            "(:destinoId IS NULL OR s.destino.id = :destinoId) AND " +
            "(:inicio IS NULL OR s.dataSolicitacao >= :inicio) AND " +
            "(:fim IS NULL OR s.dataSolicitacao <= :fim)")
    Page<Solicitacao> filtrarDinamico(
            @Param("id")          Long id,
            @Param("status")      String status,
            @Param("motoristaId") Long motoristaId,
            @Param("setorId")     Long setorId,
            @Param("destinoId")   Long destinoId,
            @Param("inicio")      LocalDate inicio,
            @Param("fim")         LocalDate fim,
            Pageable pageable
    );
}