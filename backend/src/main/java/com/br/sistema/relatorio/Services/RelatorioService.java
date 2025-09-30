package com.br.sistema.relatorio.Services;

import com.br.sistema.relatorio.DTO.RelatorioPorDiaDTO;
import com.br.sistema.relatorio.DTO.RelatorioQuantidadeDTO;
import com.br.sistema.relatorio.repository.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {

    @Autowired
    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public List<RelatorioPorDiaDTO> solicitacoesPorDia(LocalDate inicio, LocalDate fim) {
        return relatorioRepository.relatorioSolicitacoesPorDia(inicio, fim);
    }

    public List<RelatorioQuantidadeDTO> solicitacoesPorSetor() {
        return relatorioRepository.relatorioPorSetor();
    }

    public List<RelatorioQuantidadeDTO> solicitacoesPorMotorista() {
        return relatorioRepository.relatorioPorMotorista();
    }
}

