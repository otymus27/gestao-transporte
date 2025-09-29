package com.br.sistema.controllers;

import com.br.sistema.entities.Carro.DTO.CarroDTO;
import com.br.sistema.entities.Carro.DTO.CarroRequestDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.exceptions.ErrorMessage;
import com.br.sistema.services.CarroService;
import com.br.sistema.utils.AuthService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.Instant;

@RestController
@RequestMapping("/api/carros")

public class CarroController {
    private static final Logger logger = LoggerFactory.getLogger(CarroController.class);

    @Autowired
    private final CarroService carroService;
    private final AuthService authService;

    public CarroController(CarroService carroService, AuthService authService) {
        this.carroService = carroService;
        this.authService = authService;
    }

    // ✅ Cadastrar carro
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody CarroRequestDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            CarroDTO carroSalvo = carroService.cadastrar(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(carroSalvo);
        } catch (EntityExistsException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.CONFLICT.value(),
                    "Carro já existe",
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
                    "Você não tem permissão para cadastrar carros.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar carro", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao cadastrar carro.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Atualizar carro
    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CarroRequestDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            CarroDTO atualizado = carroService.atualizar(id, dto, usuarioLogado);
            return ResponseEntity.ok(atualizado);
        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Carro não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para atualizar carros.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar carro", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao atualizar carro.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Listar carros paginados (sem solicitações)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<Page<CarroDTO>> listar(Pageable pageable) {
        Page<CarroDTO> carros = carroService.listar(pageable);
        return ResponseEntity.ok(carros);
    }

    // ✅ Buscar carro por ID (inclui solicitações)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        try {
            CarroDTO carro = carroService.buscarPorId(id, true);
            return ResponseEntity.ok(carro);
        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Registro não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar registro", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar registro.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Buscar carro por placa
    @GetMapping("/buscarPorPlaca/{placa}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> buscarPorPlaca(
            @PathVariable String placa,
            HttpServletRequest request
    ) {
        try {
            CarroDTO carro = carroService.buscarPorPlaca(placa, true);
            return ResponseEntity.ok(carro);
        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Registro não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar registro", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar registro.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Excluir carro
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            carroService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Carro não encontrado",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para excluir registro",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir o registro", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao excluir registro.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
