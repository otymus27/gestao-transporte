package com.br.sistema.controllers;

import com.br.sistema.entities.Carro.DTO.CarroDetalhadoDTO;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

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
            CarroDetalhadoDTO carroSalvo = carroService.cadastrar(dto, usuarioLogado);
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
            CarroDetalhadoDTO atualizado = carroService.atualizar(id, dto, usuarioLogado);
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
    public ResponseEntity<Page<CarroDetalhadoDTO>> listar(@RequestParam(required = false) String filtro,
                                                          Pageable pageable) {
        var carros = carroService.listar(filtro, pageable);
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
            CarroDetalhadoDTO carro = carroService.buscarPorId(id, true);
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

    @GetMapping("/buscar")
    public ResponseEntity<?> filtrar(@RequestParam(required = false) String placa,
                                     @RequestParam(required = false) String marca,
                                     @RequestParam(required = false) String modelo,
                                     Pageable pageable,
                                     HttpServletRequest request) {
        try {
            var carros = carroService.filtrar(placa, marca, modelo, pageable);
            return ResponseEntity.ok(carros);

        } catch (Exception e) {
            logger.error("Erro inesperado ao filtrar carros", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar carros.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Gerar relatório (sem paginação, mas aceita filtro)
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
                resource = new InputStreamResource(carroService.exportarExcel(filtro));
                filename = "carros.xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }
            case "csv" -> {
                resource = new InputStreamResource(carroService.exportarCsv(filtro));
                filename = "carros.csv";
                mediaType = MediaType.parseMediaType("text/csv");
            }
            case "pdf" -> {
                resource = new InputStreamResource(carroService.exportarPdf(filtro));
                filename = "carros.pdf";
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
