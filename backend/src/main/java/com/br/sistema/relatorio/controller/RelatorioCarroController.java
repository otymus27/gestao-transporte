package com.br.sistema.relatorio.controller;

import com.br.sistema.relatorio.Services.RelatorioCarroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorio")
@RequiredArgsConstructor
public class RelatorioCarroController {

    private final RelatorioCarroService relatorioCarroService;

    @GetMapping("/carros/pdf")
    public ResponseEntity<byte[]> gerarRelatorioCarrosPdf() {
        byte[] relatorio = relatorioCarroService.gerarRelatorioCarrosPdf(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_carros.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(relatorio);
    }

    @GetMapping("/carros/excel")
    public ResponseEntity<byte[]> gerarRelatorioCarrosExcel() {
        byte[] relatorio = relatorioCarroService.gerarRelatorioCarrosPdf(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rel_carros.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(relatorio);
    }

}
