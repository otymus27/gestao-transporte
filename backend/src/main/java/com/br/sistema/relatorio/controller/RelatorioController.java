package com.br.sistema.relatorio.controller;

import com.br.sistema.relatorio.DTO.RelatorioPorDiaDTO;
import com.br.sistema.relatorio.DTO.RelatorioQuantidadeDTO;
import com.br.sistema.relatorio.Services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorio")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/solicitacoes-por-dia")
    public ResponseEntity<List<RelatorioPorDiaDTO>> solicitacoesPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.solicitacoesPorDia(inicio, fim));
    }

    @GetMapping("/solicitacoes-por-setor")
    public ResponseEntity<List<RelatorioQuantidadeDTO>> solicitacoesPorSetor() {
        return ResponseEntity.ok(relatorioService.solicitacoesPorSetor());
    }

    @GetMapping("/solicitacoes-por-motorista")
    public ResponseEntity<List<RelatorioQuantidadeDTO>> solicitacoesPorMotorista() {
        return ResponseEntity.ok(relatorioService.solicitacoesPorMotorista());
    }
}
