package com.br.sistema.services;

import com.br.sistema.autenticacao.SessionTracker;
import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;
import com.br.sistema.entities.Dashboard.DTO.*;
import com.br.sistema.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioRepository     usuarioRepository;
    private final CarroRepository       carroRepository;
    private final MotoristaRepository   motoristaRepository;
    private final SetorRepository       setorRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final SessionTracker        sessionTracker;

    @Transactional(readOnly = true)
    public DashboardDTO getDashboard() {

        // Totais
        long totalUsuarios     = usuarioRepository.count();
        long totalCarros       = carroRepository.count();
        long totalMotoristas   = motoristaRepository.count();
        long totalSetores      = setorRepository.count();
        long totalSolicitacoes = solicitacaoRepository.count();

        // Solicitações por status
        long solicitacoesEmAndamento = solicitacaoRepository.countByStatus("EM_ANDAMENTO");
        long solicitacoesFinalizadas = solicitacaoRepository.countByStatus("FINALIZADA");
        long solicitacoesCanceladas  = solicitacaoRepository.countByStatus("CANCELADA");

        // Usuários ativos
        long usuariosAtivosAgora = sessionTracker.getUsuariosAtivosAgora();
        long usuariosLogaramHoje = sessionTracker.getUsuariosLogaramHoje();

        // Tendência últimos 7 dias
        LocalDate hoje = LocalDate.now();
        List<SolicitacaoPorDiaDTO> solicitacoesPorDia =
                solicitacaoRepository.countByDia(hoje.minusDays(6), hoje);

        // Rankings — topCarros REMOVIDO (placa está na FichaSolicitacao)
        List<RankingItemDTO> topSetores    = solicitacaoRepository.topSetores();
        List<RankingItemDTO> topMotoristas = solicitacaoRepository.topMotoristas();

        return new DashboardDTO(
                totalUsuarios,
                totalCarros,
                totalMotoristas,
                totalSetores,
                totalSolicitacoes,
                solicitacoesEmAndamento,
                solicitacoesFinalizadas,
                solicitacoesCanceladas,
                usuariosAtivosAgora,
                usuariosLogaramHoje,
                solicitacoesPorDia,
                topSetores,
                topMotoristas
                // topCarros REMOVIDO
        );
    }
}