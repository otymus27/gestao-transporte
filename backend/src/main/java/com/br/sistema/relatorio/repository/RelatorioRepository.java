package com.br.sistema.relatorio.repository;

import com.br.sistema.relatorio.DTO.RelatorioPorDiaDTO;
import com.br.sistema.relatorio.DTO.RelatorioQuantidadeDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RelatorioRepository extends JpaRepository<Solicitacao, Long> {

    @Query("SELECT new com.br.sistema.relatorio.DTO.RelatorioPorDiaDTO(DATE(s.dataSolicitacao), COUNT(s)) " +
            "FROM Solicitacao s " +
            "WHERE s.dataSolicitacao BETWEEN :inicio AND :fim " +
            "GROUP BY DATE(s.dataSolicitacao) " +
            "ORDER BY DATE(s.dataSolicitacao)")
    List<RelatorioPorDiaDTO> relatorioSolicitacoesPorDia(LocalDate inicio, LocalDate fim);

    @Query("SELECT new com.br.sistema.relatorio.DTO.RelatorioQuantidadeDTO(s.setor.nome, COUNT(s)) " +
            "FROM Solicitacao s GROUP BY s.setor.nome")
    List<RelatorioQuantidadeDTO> relatorioPorSetor();

    @Query("SELECT new com.br.sistema.relatorio.DTO.RelatorioQuantidadeDTO(s.motorista.nome, COUNT(s)) " +
            "FROM Solicitacao s GROUP BY s.motorista.nome")
    List<RelatorioQuantidadeDTO> relatorioPorMotorista();
}

