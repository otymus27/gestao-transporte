package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Motorista.DTO.MotoristaRelatorioDTO;
import com.br.sistema.relatorio.Services.RelatorioMotoristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/motorista/relatorio")
@RequiredArgsConstructor
public class RelatorioMotoristaController {

    private final RelatorioMotoristaService relatorioMotoristaService;

    /**
     * CONSULTA Paginada (para preencher a tabela no Angular antes de exportar)
     * GET /api/motoristas/relatorio/consultar?matricula=&nome=&telefone=&ativo=
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<MotoristaRelatorioDTO>> consultarPaginado(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MotoristaRelatorioDTO> pagina =
                relatorioMotoristaService.consultarMotoristasParaRelatorioPaginado(
                        matricula, nome, telefone, ativo, page, size
                );

        return ResponseEntity.ok(pagina);
    }

    /**
     * PDF
     * GET /api/motoristas/relatorio/pdf?matricula=&nome=&telefone=&ativo=
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) Boolean ativo
    ) {
        byte[] arquivo = relatorioMotoristaService
                .gerarRelatorioMotoristasPdfFiltrado(matricula, nome, telefone, ativo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_motoristas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/motoristas/relatorio/excel?matricula=&nome=&telefone=&ativo=
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) Boolean ativo
    ) {
        byte[] arquivo = relatorioMotoristaService
                .gerarRelatorioMotoristasExcelFiltrado(matricula, nome, telefone, ativo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_motoristas.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
