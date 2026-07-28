package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Usuario.DTO.ProducaoUsuarioDTO;
import com.br.sistema.relatorio.Services.RelatorioProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Relatório de Produção por Usuário: quantidade de fichas e de solicitações
 * criadas por cada usuário em um período, para fins de comprovação de produção
 * de quem digitou os registros.
 */
@RestController
@RequestMapping("/api/producao/relatorio")
@RequiredArgsConstructor
public class RelatorioProducaoController {

    private final RelatorioProducaoService relatorioProducaoService;

    /**
     * CONSULTA Paginada (para preencher a tabela no Angular antes de exportar)
     * GET /api/producao/relatorio/consultar?nomeUsuario=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd&page=0&size=10
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<ProducaoUsuarioDTO>> consultarPaginado(
            @RequestParam(required = false) String nomeUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProducaoUsuarioDTO> pagina =
                relatorioProducaoService.consultarProducaoParaRelatorioPaginado(
                        nomeUsuario, dataInicio, dataFim, page, size
                );
        return ResponseEntity.ok(pagina);
    }

    /**
     * PDF
     * GET /api/producao/relatorio/pdf?nomeUsuario=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String nomeUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        byte[] arquivo = relatorioProducaoService
                .gerarRelatorioProducaoPdfFiltrado(nomeUsuario, dataInicio, dataFim);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_producao_usuario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/producao/relatorio/excel?nomeUsuario=&dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String nomeUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        byte[] arquivo = relatorioProducaoService
                .gerarRelatorioProducaoExcelFiltrado(nomeUsuario, dataInicio, dataFim);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_producao_usuario.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
