package com.br.sistema.controllers;

import com.br.sistema.entities.FichaSolicitacao.DTO.FichaSolicitacaoRequestDTO;
import com.br.sistema.entities.FichaSolicitacao.DTO.FichaSolicitacaoResponseDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.FichaSolicitacaoService;
import com.br.sistema.utils.AuthService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fichas")
@RequiredArgsConstructor
public class FichaSolicitacaoController {

    private static final Logger logger = LoggerFactory.getLogger(FichaSolicitacaoController.class);

    private final FichaSolicitacaoService fichaService;
    private final AuthService authService;

    // =========================================================
    // POST /api/fichas
    // Salva a ficha + todas as solicitações de uma vez só
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> salvar(@Valid @RequestBody FichaSolicitacaoRequestDTO dto,
                                    Authentication authentication,
                                    HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            FichaSolicitacaoResponseDTO resposta = fichaService.salvar(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);

        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.BAD_REQUEST, "Dado não encontrado", e.getMessage(), request);
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao salvar ficha de solicitação", e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // GET /api/fichas
    // Lista todas as fichas com paginação
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> listar(Pageable pageable, HttpServletRequest request) {
        try {
            Page<FichaSolicitacaoResponseDTO> fichas = fichaService.listar(pageable);
            return ResponseEntity.ok(fichas);
        } catch (Exception e) {
            logger.error("Erro ao listar fichas", e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // GET /api/fichas/{id}
    // Busca uma ficha com todas as solicitações
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            FichaSolicitacaoResponseDTO ficha = fichaService.buscarPorId(id);
            return ResponseEntity.ok(ficha);
        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Ficha não encontrada", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao buscar ficha id={}", id, e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // GET /api/fichas/buscar
    // Filtros opcionais: placa, período, usuário
    // =========================================================

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> buscar(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long usuarioId,
            Pageable pageable,
            HttpServletRequest request
    ) {
        try {
            Page<FichaSolicitacaoResponseDTO> resultado =
                    fichaService.filtrar(placa, inicio, fim, usuarioId, pageable);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("Erro ao filtrar fichas", e);
            return erroInterno(request);
        }
    }

    // =========================================================
    // DELETE /api/fichas/{id}
    // Exclui a ficha e todas as solicitações filhas (cascade)
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> excluir(@PathVariable Long id, HttpServletRequest request) {
        try {
            fichaService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return erro(HttpStatus.NOT_FOUND, "Ficha não encontrada", e.getMessage(), request);
        } catch (Exception e) {
            logger.error("Erro ao excluir ficha id={}", id, e);
            return erroInterno(request);
        }
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
