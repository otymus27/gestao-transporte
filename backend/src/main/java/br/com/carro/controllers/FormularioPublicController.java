package br.com.carro.controllers;

import br.com.carro.entities.DTO.formularios.*;
import br.com.carro.exceptions.ErrorMessage;
import br.com.carro.services.FormularioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class FormularioPublicController {

    private final FormularioService service;

    public FormularioPublicController(FormularioService service) {
        this.service = service;
    }

    // 📂 Explorer público - lista todas as pastas com seus formulários (com filtros e ordenação)
    @GetMapping("/explorer")
    public ResponseEntity<?> listarPastasExplorer(
            @RequestParam(required = false) String nomePasta,
            @RequestParam(required = false) String nomeArquivo,
            @RequestParam(required = false) String extensao,
            @RequestParam(required = false) Long tamanhoMin,
            @RequestParam(required = false) Long tamanhoMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataUploadInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataUploadFinal,
            @RequestParam(required = false, defaultValue = "nomeArquivo") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            HttpServletRequest httpRequest
    ) {
        try {
            ExplorerFilterRequest filter = new ExplorerFilterRequest(
                    nomePasta,
                    tamanhoMin,
                    tamanhoMax,
                    nomeArquivo,
                    extensao,
                    dataUploadInicial,
                    dataUploadFinal,
                    sortBy,
                    order
            );

            List<PastaExplorerDTO> pastas = service.listarPastasHierarquicas(filter);
            return ResponseEntity.ok(pastas);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro ao listar pastas (explorer)",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }



    // 📂 Lista todas as pastas de formulários
    @GetMapping("/pastas")
    public ResponseEntity<?> listarPastas(HttpServletRequest httpRequest) {
        try {
            List<PastaFormularioDTO> pastas = service.listarPastas();
            return ResponseEntity.ok(pastas);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Requisição inválida",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(
                            HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro ao listar pastas",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }

    // 📄 Lista todos os formulários de uma pasta
    @GetMapping("/pastas/{pastaId}/formularios")
    public ResponseEntity<?> listarFormularios(@PathVariable Long pastaId,
                                               HttpServletRequest httpRequest) {
        try {
            List<FormularioDTO> formularios = service.listarFormularios(pastaId);
            return ResponseEntity.ok(formularios);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(
                            HttpStatus.NOT_FOUND.value(),
                            "Pasta não encontrada",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Dados inválidos",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(
                            HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro ao listar formulários",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }

    // 📥 Download ou visualização de formulário
    @GetMapping("/formularios/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id,
                                      @RequestParam(defaultValue = "download") String modo,
                                      HttpServletRequest httpRequest) {
        try {
            Resource resource = service.downloadFormulario(id);

            // 🔎 Detecta o tipo MIME real do arquivo
            String contentType;
            try {
                contentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
            } catch (IOException e) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // fallback
            }

            // 📌 Decide o Content-Disposition com base no modo
            String disposition = "inline".equalsIgnoreCase(modo)
                    ? "inline"  // abre no navegador
                    : "attachment"; // força download

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            disposition + "; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(
                            HttpStatus.NOT_FOUND.value(),
                            "Formulário não encontrado",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Não foi possível realizar o download",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro de I/O",
                            "Falha ao ler o arquivo do disco: " + e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro inesperado",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }


}
