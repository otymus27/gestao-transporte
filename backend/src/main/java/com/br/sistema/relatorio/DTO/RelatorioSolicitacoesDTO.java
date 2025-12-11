package com.br.sistema.relatorio.DTO;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResponseDTO;

import java.util.List;

public record RelatorioSolicitacoesDTO(
        PaginadoDTO<SolicitacaoResponseDTO> solicitacoes,
        List<QuantidadePorDiaDTO> quantidadePorDia,
        List<QuantidadePorMesDTO> quantidadePorMes,
        Integer kmTotalGeral
) {}
