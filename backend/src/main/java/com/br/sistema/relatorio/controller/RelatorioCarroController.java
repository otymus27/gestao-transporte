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

    @GetMapping(value = "/carros", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarRelatorioCarros() {
        byte[] pdf = relatorioCarroService.gerarRelatorioCarrosSimples();

        String fileName = "relatorio_carros_simples.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
