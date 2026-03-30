package com.br.sistema.repositories;

import aj.org.objectweb.asm.commons.Remapper;
import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;
import com.br.sistema.entities.Dashboard.DTO.RankingItemDTO;
import com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO;
import com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoPorStatusDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO;
import com.br.sistema.entities.Usuario.Usuario;
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
    // CONSULTAS BÁSICAS (PAGINADAS)
    // =========================

    // 🔍 Buscar por status (ex.: todas as PENDENTES)
    Page<Solicitacao> findByStatus(String status, Pageable pageable);

    // 🔍 Buscar por setor (id)
    Page<Solicitacao> findBySetor_Id(Long setorId, Pageable pageable);

    // 🔍 Buscar por motorista (id)
    Page<Solicitacao> findByMotorista_Id(Long motoristaId, Pageable pageable);

    // 🔍 Buscar por carro (id)
    Page<Solicitacao> findByCarro_Id(Long carroId, Pageable pageable);

    // 🔍 Buscar por usuário (id)
    Page<Solicitacao> findByUsuario_Id(Long usuarioId, Pageable pageable);

    // 🔍 Buscar por destino (id)
    Page<Solicitacao> findByDestino_Id(Long destinoId, Pageable pageable);

    // ✅ caso você precise filtrar por id em página (não é comum, mas mantém coerência)
    Page<Solicitacao> findAllById(Long id, Pageable pageable);

    // 🔍 Buscar por solicitação (id)
    Page<Solicitacao> findById(Long id, Pageable pageable);

    @Query("SELECT s FROM Solicitacao s WHERE LOWER(s.usuario.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Solicitacao> findByUsuarioNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

    @Query("SELECT s FROM Solicitacao s WHERE LOWER(s.usuario.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Solicitacao> findByUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);

    long countByStatus(String status);

    // =========================
    // DASHBOARD / AGRUPAMENTOS
    // =========================

    // 📊 Tendência por dia (intervalo)
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



    // 📊 Ranking por Setor
    @Query("""
           SELECT new com.br.sistema.entities.Dashboard.DTO.RankingItemDTO(
               se.nome, COUNT(s)
           )
           FROM Solicitacao s
           JOIN s.setor se
           GROUP BY se.nome
           ORDER BY COUNT(s) DESC
           """)
    List<RankingItemDTO> topSetores();

    // 📊 Ranking por Motorista
    @Query("""
           SELECT new com.br.sistema.entities.Dashboard.DTO.RankingItemDTO(
               m.nome, COUNT(s)
           )
           FROM Solicitacao s
           JOIN s.motorista m
           GROUP BY m.nome
           ORDER BY COUNT(s) DESC
           """)
    List<RankingItemDTO> topMotoristas();

    // 📊 Ranking por Carro
    @Query("""
           SELECT new com.br.sistema.entities.Dashboard.DTO.RankingItemDTO(
               c.placa, COUNT(s)
           )
           FROM Solicitacao s
           JOIN s.carro c
           GROUP BY c.placa
           ORDER BY COUNT(s) DESC
           """)
    List<RankingItemDTO> topCarros();

    // ✅ Solicitações por motorista
    @Query("""
        SELECT new com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO(
            m.id, m.nome, COUNT(s)
        )
        FROM Solicitacao s
        JOIN s.motorista m
        GROUP BY m.id, m.nome
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorMotoristaDTO> buscarPorMotorista();

    // ✅ Solicitações por setor
    @Query("""
        SELECT new com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO(
            se.id, se.nome, COUNT(s)
        )
        FROM Solicitacao s
        JOIN s.setor se
        GROUP BY se.id, se.nome
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorSetorDTO> buscarPorSetor();

    // ✅ Solicitações por usuário
    @Query("""
        SELECT new com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO(
            u.id, u.nome, COUNT(s)
        )
        FROM Solicitacao s
        JOIN s.usuario u
        GROUP BY u.id, u.nome
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorUsuarioDTO> buscarPorUsuario();

    // ✅ Solicitações por status
    @Query("""
        SELECT new com.br.sistema.entities.Solicitacao.DTO.SolicitacaoPorStatusDTO(
            s.status, COUNT(s)
        )
        FROM Solicitacao s
        GROUP BY s.status
        ORDER BY COUNT(s) DESC
    """)
    List<SolicitacaoPorStatusDTO> buscarPorStatus();

    // ✅ Solicitações por intervalo de datas (agrupadas por dia)
    @Query("""
        SELECT new com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO(
            DATE(s.dataSolicitacao), COUNT(s)
        )
        FROM Solicitacao s
        WHERE DATE(s.dataSolicitacao) BETWEEN :inicio AND :fim
        GROUP BY DATE(s.dataSolicitacao)
        ORDER BY DATE(s.dataSolicitacao)
    """)
    List<SolicitacaoPorDiaDTO> buscarPorDatas(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // =========================
    // RELATÓRIO (LISTA - FILTRO "LIVRE")
    // =========================

    /**
     * Responsável pelos filtros para gerar o relatório (sem paginação).
     * Usa JOIN FETCH para não estourar LazyInitialization no Jasper.
     * Usa DISTINCT para evitar linhas duplicadas em caso de joins.
     */
    @Query("""
        SELECT DISTINCT s
        FROM Solicitacao s
        JOIN FETCH s.carro c
        JOIN FETCH s.motorista m
        JOIN FETCH s.usuario u
        JOIN FETCH s.setor se
        JOIN FETCH s.destino d
        WHERE (:filtro IS NULL OR :filtro = '' OR
                LOWER(c.marca)  LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(c.modelo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(c.placa)  LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(m.nome)   LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(u.nome)   LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(se.nome)  LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(d.nome)   LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                LOWER(s.status) LIKE LOWER(CONCAT('%', :filtro, '%'))
          )
          ORDER BY s.id DESC
    """)
    List<Solicitacao> filtrarSemPaginacao(@Param("filtro") String filtro);


    /**
     * Se o seu campo dataSolicitacao for LocalDate (sem horário),
     * você pode manter este também (ou trocar conforme o tipo do atributo).
     */
    Page<Solicitacao> findByDataSolicitacaoBetween(LocalDate inicio, LocalDate fim, Pageable pageable);

    // =========================
    // SÉRIE MENSAL (RELATÓRIO / GRÁFICO)
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

    @Query("SELECT s FROM Solicitacao s WHERE " +
            "(:id IS NULL OR s.id = :id) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:motoristaId IS NULL OR s.motorista.id = :motoristaId) AND " +
            "(:carroId IS NULL OR s.carro.id = :carroId) AND " +
            "(:setorId IS NULL OR s.setor.id = :setorId) AND " +
            "(:username IS NULL OR LOWER(s.usuario.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:destinoId IS NULL OR s.destino.id = :destinoId) AND " +
            "(:inicio IS NULL OR s.dataSolicitacao >= :inicio) AND " +
            "(:fim IS NULL OR s.dataSolicitacao <= :fim)")
    Page<Solicitacao> filtrarDinamico(@Param("id") Long id,
                                      @Param("status") String status,
                                      @Param("motoristaId") Long motoristaId,
                                      @Param("carroId") Long carroId,
                                      @Param("setorId") Long setorId,
                                      @Param("username") String username,
                                      @Param("destinoId") Long destinoId,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim,
                                      Pageable pageable);

}
