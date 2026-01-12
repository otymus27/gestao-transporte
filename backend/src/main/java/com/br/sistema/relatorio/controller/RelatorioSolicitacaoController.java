package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRelatorioDTO;
import com.br.sistema.relatorio.Services.RelatorioSolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/solicitacao/relatorio")
@RequiredArgsConstructor
public class RelatorioSolicitacaoController {

    private final RelatorioSolicitacaoService relatorioSolicitacaoService;

    /**
     * CONSULTA Paginada (para preencher a tabela no Angular antes de exportar)
     * GET /api/solicitacao/relatorio/consultar?filtro=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<SolicitacaoRelatorioDTO>> consultarPaginado(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SolicitacaoRelatorioDTO> pagina =
                relatorioSolicitacaoService.consultarSolicitacoesParaRelatorioPaginado(
                        filtro, dataInicio, dataFim, page, size
                );

        return ResponseEntity.ok(pagina);
    }

    /**
     * PDF
     * GET /api/solicitacao/relatorio/pdf?filtro=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        byte[] arquivo = relatorioSolicitacaoService
                .gerarRelatorioSolicitacoesPdfFiltrado(filtro, dataInicio, dataFim);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_solicitacoes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/solicitacao/relatorio/excel?filtro=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        byte[] arquivo = relatorioSolicitacaoService
                .gerarRelatorioSolicitacoesExcelFiltrado(filtro, dataInicio, dataFim);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_solicitacoes.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
