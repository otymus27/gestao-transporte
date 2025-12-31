package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Destino.DTO.DestinoRelatorioDTO;
import com.br.sistema.relatorio.Services.RelatorioDestinoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/destino/relatorio")
@RequiredArgsConstructor
public class RelatorioDestinoController {

    private final RelatorioDestinoService relatorioDestinoService;

    /**
     * CONSULTA Paginada (para preencher a tabela no Angular antes de exportar)
     * GET /api/destino/relatorio/consultar?nome=
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<DestinoRelatorioDTO>> consultarPaginado(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<DestinoRelatorioDTO> pagina =
                relatorioDestinoService.consultarDestinosParaRelatorioPaginado(
                        nome, page, size
                );

        return ResponseEntity.ok(pagina);
    }

    /**
     * PDF
     * GET /api/destino/relatorio/pdf?nome=
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String nome
    ) {
        byte[] arquivo = relatorioDestinoService.gerarRelatorioDestinosPdfFiltrado(nome);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_destino.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/destino/relatorio/excel?nome=
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String nome
    ) {
        byte[] arquivo = relatorioDestinoService.gerarRelatorioDestinosExcelFiltrado(nome);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_destino.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
