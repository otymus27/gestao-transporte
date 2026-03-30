package com.br.sistema.controllers;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoDetalhadaDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRequestDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResponseDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.SolicitacaoService;
import com.br.sistema.utils.AuthService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/solicitacao")
public class SolicitacaoController {

    private static final Logger logger = LoggerFactory.getLogger(SolicitacaoController.class);

    private final SolicitacaoService solicitacaoService;
    private final AuthService authService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, AuthService authService) {
        this.solicitacaoService = solicitacaoService;
        this.authService = authService;
    }

    // =========================================================
    // CRUD
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody SolicitacaoRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SolicitacaoResponseDTO salva = solicitacaoService.cadastrar(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);

        } catch (EntityExistsException e) {
            return erro(HttpStatus.CONFLICT, "Solicitação já existe", e.getMessage(), request);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao cadastrar solicitação", e);
            return erroInterno(request);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody SolicitacaoRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SolicitacaoResponseDTO atualizada = solicitacaoService.atualizar(id, dto, usuarioLogado);
            return ResponseEntity.ok(atualizada);

        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada", e.getMessage(), request);
        } catch (EntityExistsException e) {
            return erro(HttpStatus.CONFLICT, "Duplicidade", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao atualizar solicitação id={}", id, e);
            return erroInterno(request);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id,
                                     Authentication authentication,
                                     HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            solicitacaoService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao deletar solicitação id={}", id, e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // ATUALIZAÇÃO DE STATUS (aprovar / recusar)
    // PATCH /api/solicitacao/{id}/status?valor=APROVADA
    // =========================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id,
                                             @RequestParam String valor,
                                             Authentication authentication,
                                             HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            SolicitacaoResponseDTO atualizada = solicitacaoService.atualizarStatus(id, valor, usuarioLogado);
            return ResponseEntity.ok(atualizada);

        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status da solicitação id={}", id, e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> listar(Pageable pageable, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(solicitacaoService.listarTodosPaginado(pageable));
        } catch (Exception e) {
            logger.error("Erro ao listar solicitações", e);
            return erroInterno(request);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao buscar solicitação id={}", id, e);
            return erroInterno(request);
        }
    }

    @GetMapping("/detalhada/{id}")
    public ResponseEntity<?> buscarDetalhada(@PathVariable Long id, HttpServletRequest request) {
        try {
            SolicitacaoDetalhadaDTO solicitacao = solicitacaoService.buscarDetalhado(id);
            return ResponseEntity.ok(solicitacao);
        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao buscar solicitação detalhada id={}", id, e);
            return erroInterno(request);
        }
    }

    /**
     * Endpoint único de busca com filtros opcionais combinados.
     * carroId REMOVIDO — placa agora está na FichaSolicitacao.
     *
     * Exemplos:
     *   GET /api/solicitacao/buscar?status=PENDENTE
     *   GET /api/solicitacao/buscar?motoristaId=3&setorId=1
     *   GET /api/solicitacao/buscar?inicio=2024-01-01&fim=2024-12-31
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long motoristaId,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable,
            HttpServletRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    solicitacaoService.filtrarGenerico(
                            id, status, motoristaId, setorId, username, destinoId, inicio, fim, pageable
                    )
            );
        } catch (Exception e) {
            logger.error("Erro ao filtrar solicitações", e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // CONSULTAS PARA DASHBOARDS
    // =========================================================

    @GetMapping("/consulta/intervalo")
    public ResponseEntity<?> porIntervalo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
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

    // =========================================================
    // HELPERS
    // =========================================================

    private ResponseEntity<ErrorMessage> erro(HttpStatus status, String erro,
                                              String mensagem, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorMessage(status.value(), erro, mensagem, request.getRequestURI()));
    }

    private ResponseEntity<ErrorMessage> erroInterno(HttpServletRequest request) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor", "Ocorreu um erro inesperado.", request);
    }
}