package com.br.sistema.controllers;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRequestDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResponseDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoDetalhadaDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.SolicitacaoService;
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
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/api/solicitacao")
public class SolicitacaoController {

    private static final Logger logger = LoggerFactory.getLogger(SolicitacaoController.class);

    private final SolicitacaoService solicitacaoService;
    private final AuthService authService;

    @Autowired
    public SolicitacaoController(SolicitacaoService solicitacaoService, AuthService authService) {
        this.solicitacaoService = solicitacaoService;
        this.authService = authService;
    }

    // ✅ Cadastrar solicitação
    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody SolicitacaoRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SolicitacaoResponseDTO solicitacaoSalva = solicitacaoService.cadastrar(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoSalva);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.CONFLICT.value(),
                    "Solicitação já existe", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para cadastrar solicitações.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar solicitação", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao cadastrar solicitação.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Atualizar solicitação
    @PatchMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE', 'BASIC')")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody SolicitacaoRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SolicitacaoResponseDTO solicitacaoAtualizada = solicitacaoService.atualizar(id, dto, usuarioLogado);
            return ResponseEntity.ok(solicitacaoAtualizada);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Solicitação não encontrada", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.CONFLICT.value(),
                    "Duplicidade", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para atualizar solicitações.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar solicitação", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao atualizar solicitação.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Deletar solicitação
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id,
                                     Authentication authentication,
                                     HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            solicitacaoService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Solicitação não encontrada", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                    "Acesso negado", "Você não tem permissão para deletar solicitações.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar solicitação", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao deletar solicitação.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Listar solicitações (paginado)
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listar(Pageable pageable, HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.listarTodosPaginado(pageable);
            return ResponseEntity.ok(solicitacoes);

        } catch (Exception e) {
            logger.error("Erro inesperado ao listar solicitações", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao listar solicitações.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar solicitação por id (resumido)
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            SolicitacaoResponseDTO solicitacao = solicitacaoService.buscarPorId(id);
            return ResponseEntity.ok(solicitacao);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Solicitação não encontrada", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar solicitação resumida", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao buscar solicitação.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar solicitação detalhada
    @GetMapping("/detalhada/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarDetalhada(@PathVariable Long id, HttpServletRequest request) {
        try {
            SolicitacaoDetalhadaDTO solicitacao = solicitacaoService.buscarDetalhado(id);
            return ResponseEntity.ok(solicitacao);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                    "Solicitação não encontrada", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar solicitação detalhada", e);
            ErrorMessage error = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor", "Erro ao buscar solicitação detalhada.", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar por status
    @GetMapping("/buscar/status")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorStatus(@RequestParam String status,
                                              Pageable pageable,
                                              HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.filtrarPorStatus(status, pageable);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro ao filtrar por status", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações por status.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar por motorista
    @GetMapping("/buscar/motorista/{motoristaId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorMotorista(@PathVariable Long motoristaId,
                                                 Pageable pageable,
                                                 HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.filtrarPorMotorista(motoristaId, pageable);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro ao filtrar por motorista", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações por motorista.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar por carro
    @GetMapping("/buscar/carro/{carroId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorCarro(@PathVariable Long carroId,
                                             Pageable pageable,
                                             HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.filtrarPorCarro(carroId, pageable);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro ao filtrar por carro", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações por carro.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar por setor
    @GetMapping("/buscar/setor/{setorId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorSetor(@PathVariable Long setorId,
                                             Pageable pageable,
                                             HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.filtrarPorSetor(setorId, pageable);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro ao filtrar por setor", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações por setor.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar por usuário
    @GetMapping("/buscar/usuario/{usuarioId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorUsuario(@PathVariable Long usuarioId,
                                               Pageable pageable,
                                               HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.filtrarPorUsuario(usuarioId, pageable);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro ao filtrar por usuário", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações por usuário.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Filtrar por destino
    @GetMapping("/buscar/destino/{destinoId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarPorDestino(@PathVariable Long destinoId,
                                               Pageable pageable,
                                               HttpServletRequest request) {
        try {
            var solicitacoes = solicitacaoService.filtrarPorDestino(destinoId, pageable);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro ao filtrar por destino", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações por destino.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Endpoint genérico de busca
    @GetMapping("/buscar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> filtrarGenerico(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long motoristaId,
            @RequestParam(required = false) Long carroId,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable,
            HttpServletRequest request
    ) {
        try {
            var solicitacoes = solicitacaoService.filtrarGenerico(
                    id,status, motoristaId, carroId, setorId, username, destinoId, inicio,fim, pageable
            );
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            logger.error("Erro inesperado ao filtrar solicitações", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar solicitações com filtros.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/consulta/intervalo")
    public ResponseEntity<?> porIntervalo(@RequestParam LocalDate inicio,
                                          @RequestParam LocalDate fim) {
        return ResponseEntity.ok(solicitacaoService.buscarPorIntervalo(inicio, fim));
    }

    @GetMapping("/consulta/motoristas")
    public ResponseEntity<?> porMotorista() {
        return ResponseEntity.ok(solicitacaoService.buscarPorMotorista());
    }

    @GetMapping("/consulta/setor")
    public ResponseEntity<?> porSetor() {
        return ResponseEntity.ok(solicitacaoService.buscarPorSetor());
    }

    @GetMapping("/consulta/usuario")
    public ResponseEntity<?> porUsuario() {
        return ResponseEntity.ok(solicitacaoService.buscarPorUsuario());
    }

    @GetMapping("/consulta/status")
    public ResponseEntity<?> porStatus() {
        return ResponseEntity.ok(solicitacaoService.buscarPorStatus());
    }

    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportarRelatorio(
            @RequestParam(required = false) String filtro,
            @RequestParam(defaultValue = "pdf") String tipo
    ) throws IOException {

        InputStreamResource resource;
        String filename;
        MediaType mediaType;

        switch (tipo.toLowerCase()) {
            case "excel" -> {
                resource = new InputStreamResource(solicitacaoService.exportarExcel(filtro));
                filename = "solicitacoes.xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }
            case "csv" -> {
                resource = new InputStreamResource(solicitacaoService.exportarCsv(filtro));
                filename = "solicitacoes.csv";
                mediaType = MediaType.parseMediaType("text/csv");
            }
            case "pdf" -> {
                resource = new InputStreamResource(solicitacaoService.exportarPdf(filtro));
                filename = "solicitacoes.pdf";
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
