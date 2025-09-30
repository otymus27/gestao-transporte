package com.br.sistema.entities.Dashboard.DTO;

import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;

import java.util.List;

public record DashboardDTO(
        // Totais gerais
        long totalUsuarios,
        long totalCarros,
        long totalMotoristas,
        long totalSetores,
        long totalSolicitacoes,

        // Estatísticas de solicitações
        long solicitacoesEmAndamento,
        long solicitacoesFinalizadas,
        long solicitacoesCanceladas,

        // 📊 Usuários
        long usuariosAtivosAgora,
        long usuariosLogaramHoje,

        // Tendência
        List<SolicitacaoPorDiaDTO> solicitacoesPorDia,

        // Ranking
        List<RankingItemDTO> topSetores,
        List<RankingItemDTO> topMotoristas,
        List<RankingItemDTO> topCarros
) {}

