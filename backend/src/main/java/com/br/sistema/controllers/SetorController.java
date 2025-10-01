package com.br.sistema.controllers;

import com.br.sistema.entities.Setor.DTO.SetorDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SetorRequestDTO;
import com.br.sistema.entities.Setor.DTO.SetorResponseDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.SetorService;
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
@RequestMapping("/api/setor")
public class SetorController {

    private static final Logger logger = LoggerFactory.getLogger(SetorController.class);

    @Autowired
    private final SetorService setorService;
    private final AuthService authService;

    public SetorController(SetorService setorService, AuthService authService) {
        this.setorService = setorService;
        this.authService = authService;
    }

    // ✅ Cadastrar setor
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody SetorRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SetorResponseDTO setorSalvo = setorService.cadastrar(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(setorSalvo);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.CONFLICT.value(),
                    "Setor já existe", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para cadastrar setores.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar setor", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao cadastrar setor.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Atualizar setor
    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody SetorRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SetorResponseDTO setorAtualizado = setorService.atualizar(id, dto, usuarioLogado);
            return ResponseEntity.ok(setorAtualizado);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Setor não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.CONFLICT.value(),
                    "Duplicidade", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para atualizar setores.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar setor", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao atualizar setor.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Deletar setor
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id,
                                     Authentication authentication,
                                     HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            setorService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Setor não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para deletar setores.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar setor", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao deletar setor.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Listar setores (paginado)
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listar(Pageable pageable, HttpServletRequest request) {
        try {
            var setores = setorService.listar(pageable);
            return ResponseEntity.ok(setores);

        } catch (Exception e) {
            logger.error("Erro inesperado ao listar setores", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao listar setores.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar setor por id (resumido)
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            SetorResponseDTO setor = new SetorResponseDTO(
                    setorService.buscarPorId(id, false).id(),
                    setorService.buscarPorId(id, false).nome()
            );
            return ResponseEntity.ok(setor);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Setor não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar setor resumido", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao buscar setor.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar setor detalhado
    @GetMapping("/detalhado/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarDetalhado(@PathVariable Long id, HttpServletRequest request) {
        try {
            SetorDetalhadoDTO setor = setorService.buscarPorId(id, true);
            return ResponseEntity.ok(setor);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Setor não encontrado", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar setor detalhado", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao buscar setor detalhado.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar setores por parte do nome (paginado)
    @GetMapping("/buscar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorNome(@RequestParam String nome,
                                            Pageable pageable,
                                            HttpServletRequest request) {
        try {
            var setores = setorService.filtrarPorNome(nome, pageable);
            return ResponseEntity.ok(setores);

        } catch (Exception e) {
            logger.error("Erro inesperado ao filtrar setores por nome", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar setores por nome.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/relatorio/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarExcel(@RequestParam(required = false) String filtro) throws IOException {
        var stream = setorService.exportarExcel(filtro);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=setores.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/relatorio/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarCsv(@RequestParam(required = false) String filtro) {
        var stream = setorService.exportarCsv(filtro);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=setores.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping("/relatorio/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarPdf(@RequestParam(required = false) String filtro) {
        var stream = setorService.exportarPdf(filtro);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=setores.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


}
