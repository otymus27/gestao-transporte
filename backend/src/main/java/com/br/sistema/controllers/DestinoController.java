package com.br.sistema.controllers;

import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Destino.DTO.DestinoRequestDTO;
import com.br.sistema.entities.Destino.DTO.DestinoResponseDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.DestinoService;
import com.br.sistema.utils.AuthService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/destino")
public class DestinoController {

    private static final Logger logger = LoggerFactory.getLogger(DestinoController.class);

    @Autowired
    private final DestinoService destinoService;
    private final AuthService authService;

    public DestinoController(DestinoService destinoService, AuthService authService) {
        this.destinoService = destinoService;
        this.authService = authService;
    }

    // ✅ Cadastrar destino
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody DestinoRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            DestinoResponseDTO destinoSalvo = destinoService.cadastrar(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(destinoSalvo);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.CONFLICT.value(),
                    "Destino já existe", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para cadastrar destinos.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar destino", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao cadastrar destino.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Atualizar destino
    @PatchMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody DestinoRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            DestinoResponseDTO destinoAtualizado = destinoService.atualizar(id, dto, usuarioLogado);
            return ResponseEntity.ok(destinoAtualizado);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Destino não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.CONFLICT.value(),
                    "Duplicidade", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para atualizar destinos.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar destino", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao atualizar destino.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Deletar destino
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id,
                                     Authentication authentication,
                                     HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            destinoService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Destino não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para deletar destinos.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar destino", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao deletar destino.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Listar destinos (paginado)
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listar(Pageable pageable, HttpServletRequest request) {
        try {
            var destinos = destinoService.listar(pageable);
            return ResponseEntity.ok(destinos);

        } catch (Exception e) {
            logger.error("Erro inesperado ao listar destinos", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao listar destinos.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar destino por id (resumido)
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            DestinoResponseDTO destino = new DestinoResponseDTO(
                    destinoService.buscarPorId(id, false).id(),
                    destinoService.buscarPorId(id, false).nome()
            );
            return ResponseEntity.ok(destino);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Destino não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar destino resumido", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao buscar destino.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar destino detalhado
    @GetMapping("/detalhado/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarDetalhado(@PathVariable Long id, HttpServletRequest request) {
        try {
            DestinoDetalhadoDTO destino = destinoService.buscarPorId(id, true);
            return ResponseEntity.ok(destino);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Destino não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar destino detalhado", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao buscar destino detalhado.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar destinos por parte do nome (paginado)
    @GetMapping("/buscar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorNome(@RequestParam String nome,
                                            Pageable pageable,
                                            HttpServletRequest request) {
        try {
            var destinos = destinoService.filtrarPorNome(nome, pageable);
            return ResponseEntity.ok(destinos);

        } catch (Exception e) {
            logger.error("Erro inesperado ao filtrar destinos por nome", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar destinos por nome.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Exportar Excel
    @GetMapping("/relatorio/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarExcel(
            @RequestParam(required = false) String filtro
    ) throws IOException {
        var stream = destinoService.exportarExcel(filtro);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=destinos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    // ✅ Exportar CSV
    @GetMapping("/relatorio/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarCsv(
            @RequestParam(required = false) String filtro
    ) {
        var stream = destinoService.exportarCsv(filtro);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=destinos.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    // ✅ Exportar PDF
    @GetMapping("/relatorio/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarPdf(
            @RequestParam(required = false) String filtro
    ) {
        var stream = destinoService.exportarPdf(filtro);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=destinos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }




    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarRelatorio(
            @RequestParam(required = false) String filtro,
            @RequestParam(defaultValue = "pdf") String tipo // pdf | csv | excel
    ) throws IOException {

        InputStreamResource resource;
        String filename;
        MediaType mediaType;

        switch (tipo.toLowerCase()) {
            case "excel" -> {
                resource = new InputStreamResource(destinoService.exportarExcel(filtro));
                filename = "destinos.xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }
            case "csv" -> {
                resource = new InputStreamResource(destinoService.exportarCsv(filtro));
                filename = "destinos.csv";
                mediaType = MediaType.parseMediaType("text/csv");
            }
            case "pdf" -> {
                resource = new InputStreamResource(destinoService.exportarPdf(filtro));
                filename = "destinos.pdf";
                mediaType = MediaType.APPLICATION_PDF;
            }
            default -> throw new IllegalArgumentException("Tipo de relatório inválido: " + tipo);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(resource);
    }
}
