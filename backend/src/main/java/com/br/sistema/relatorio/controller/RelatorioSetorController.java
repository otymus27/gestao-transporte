package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO;
import com.br.sistema.relatorio.Services.RelatorioSetorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setor/relatorio")
@RequiredArgsConstructor
public class RelatorioSetorController {

    private final RelatorioSetorService relatorioSetorService;

    /**
     * CONSULTA Paginada (para preencher a tabela no Angular antes de exportar)
     * GET /api/setor/relatorio/consultar?nome=
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<SetorRelatorioDTO>> consultarPaginado(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SetorRelatorioDTO> pagina =
                relatorioSetorService.consultarSetoresParaRelatorioPaginado(
                        nome, page, size
                );

        return ResponseEntity.ok(pagina);
    }

    /**
     * PDF
     * GET /api/setor/relatorio/pdf?nome=
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String nome
    ) {
        byte[] arquivo = relatorioSetorService.gerarRelatorioSetoresPdfFiltrado(nome);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_setor.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/setor/relatorio/excel?nome=
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String nome
    ) {
        byte[] arquivo = relatorioSetorService.gerarRelatorioSetoresExcelFiltrado(nome);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_setor.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
