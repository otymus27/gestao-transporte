package com.br.sistema.relatorio.controller;

import com.br.sistema.entities.Carro.DTO.CarroRelatorioDTO;
import com.br.sistema.relatorio.Services.RelatorioCarroService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carros/relatorio")
@RequiredArgsConstructor
public class RelatorioCarroController {

    private final RelatorioCarroService relatorioCarroService;

    /**
     * CONSULTA Paginada (para preencher a tabela no Angular antes de exportar)
     ** GET /api/carros/relatorio/consultar?placa=&marca=&modelo=&tipo=
     */
    @GetMapping("/consultar")
    public ResponseEntity<Page<CarroRelatorioDTO>> consultarPaginado(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CarroRelatorioDTO> pagina =
                relatorioCarroService.consultarCarrosParaRelatorioPaginado(placa, marca, modelo, tipo, page, size);

        return ResponseEntity.ok(pagina);
    }

    /**
     * PDF
     * GET /api/relatorio/carros/pdf?placa=&marca=&modelo=&tipo=
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String tipo
    ) {
        byte[] arquivo = relatorioCarroService.gerarRelatorioCarrosPdfFiltrado(placa, marca, modelo, tipo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_carros.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    /**
     * EXCEL
     * GET /api/relatorio/carros/excel?placa=&marca=&modelo=&tipo=
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String tipo
    ) {
        byte[] arquivo = relatorioCarroService.gerarRelatorioCarrosExcelFiltrado(placa, marca, modelo, tipo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_carros.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
