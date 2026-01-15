package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRelatorioDTO;
import com.br.sistema.relatorio.Services.RelatorioSolicitacaoService;
import com.br.sistema.relatorio.enums.TipoRelatorioSolicitacao;
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
     * GET /api/solicitacao/relatorio/consultar?filtro=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd&page=0&size=10
     *
     * Obs.: a consulta paginada é "neutra" (não depende do tipo),
     * pois o tipo impacta o template/ordenação do PDF/Excel.
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<SolicitacaoRelatorioDTO>> consultarPaginado(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
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
     * GET /api/solicitacao/relatorio/pdf?tipo=POR_SETOR&filtro=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     *
     * Se "tipo" não for enviado, assume POR_SETOR (compatível com a versão antiga do frontend).
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) TipoRelatorioSolicitacao tipo,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        TipoRelatorioSolicitacao tipoFinal = (tipo != null) ? tipo : TipoRelatorioSolicitacao.POR_SETOR;

        byte[] arquivo = relatorioSolicitacaoService
                .gerarRelatorioSolicitacoesPdfFiltrado(tipoFinal, filtro, dataInicio, dataFim);

        String filename = "rel_solicitacoes_" + tipoFinal.name().toLowerCase() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/solicitacao/relatorio/excel?tipo=POR_SETOR&filtro=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     *
     * Se "tipo" não for enviado, assume POR_SETOR (compatível com a versão antiga do frontend).
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) TipoRelatorioSolicitacao tipo,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        TipoRelatorioSolicitacao tipoFinal = (tipo != null) ? tipo : TipoRelatorioSolicitacao.POR_SETOR;

        byte[] arquivo = relatorioSolicitacaoService
                .gerarRelatorioSolicitacoesExcelFiltrado(tipoFinal, filtro, dataInicio, dataFim);

        String filename = "rel_solicitacoes_" + tipoFinal.name().toLowerCase() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
