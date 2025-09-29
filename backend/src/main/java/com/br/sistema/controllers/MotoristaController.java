package com.br.sistema.controllers;

import com.br.sistema.entities.Motorista.DTO.MotoristaRequestDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.MotoristaService;
import com.br.sistema.utils.AuthService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;


@RestController
@RequestMapping("/api/motorista")
@RequiredArgsConstructor
public class MotoristaController {

    private static final Logger logger = LoggerFactory.getLogger(MotoristaController.class);

    private final MotoristaService motoristaService;
    private final AuthService authService;

    // ✅ Cadastrar motorista
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody MotoristaRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request)  {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);

            MotoristaDTO motoristaSalvo = motoristaService.cadastrar(dto, usuarioLogado);

            return ResponseEntity.status(HttpStatus.CREATED).body(motoristaSalvo);

        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.CONFLICT.value(),
                    "Motorista já existe",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para cadastrar motoristas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar motorista", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao cadastrar motorista.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Atualizar motorista
    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody MotoristaRequestDTO dto,
                                       Authentication authentication,
                                       HttpServletRequest request){
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);

            MotoristaDTO motoristaAtualizado = motoristaService.atualizar(id, dto, usuarioLogado);

            return ResponseEntity.ok(motoristaAtualizado);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Motorista não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para atualizar motoristas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar motorista", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao atualizar motorista.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Deletar motorista
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id,
                                     Authentication authentication,
                                     HttpServletRequest request)  {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);

            motoristaService.deletar(id, usuarioLogado);

            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Motorista não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para deletar motoristas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar motorista", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao deletar motorista.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Listar motoristas com paginação
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarTodos(Pageable pageable, HttpServletRequest request) {
        try {
            var motoristas = motoristaService.listarTodosPaginado(pageable);
            return ResponseEntity.ok(motoristas);

        } catch (Exception e) {
            logger.error("Erro inesperado ao listar motoristas", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao listar motoristas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar motorista por id sem detalhes
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            MotoristaDTO motorista = motoristaService.buscarPorId(id);
            return ResponseEntity.ok(motorista);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Motorista não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar motorista resumido", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar motorista resumido.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    // ✅ Buscar motorista detalhado
    @GetMapping("/detalhado/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarDetalhado(@PathVariable Long id, HttpServletRequest request) {
        try {
            MotoristaDTO motorista = motoristaService.buscarDetalhado(id);
            return ResponseEntity.ok(motorista);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Motorista não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar motorista detalhado", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar motorista detalhado.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }




}
