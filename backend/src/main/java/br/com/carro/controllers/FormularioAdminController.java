package br.com.carro.controllers;

import br.com.carro.entities.DTO.formularios.CriarPastaFormularioRequest;
import br.com.carro.entities.DTO.formularios.FormularioDTO;
import br.com.carro.entities.DTO.formularios.PastaFormularioDTO;
import br.com.carro.entities.DTO.formularios.UploadFormularioResponse;
import br.com.carro.entities.Formulario;
import br.com.carro.entities.PastaFormulario;
import br.com.carro.exceptions.ErrorMessage;
import br.com.carro.repositories.FormularioRepository;
import br.com.carro.services.FormularioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class FormularioAdminController {

    private final FormularioService service;
    private final FormularioRepository formularioRepository;


    public FormularioAdminController(FormularioService service, FormularioRepository formularioRepository) {
        this.service = service;
        this.formularioRepository = formularioRepository;
    }

    // 📂 Criar nova pasta de formulários
    @PostMapping("/pastas")
    public ResponseEntity<?> criarPasta(@RequestBody CriarPastaFormularioRequest req,
                                        Authentication authentication,
                                        HttpServletRequest httpRequest) {

        if (req == null || req.nomePasta() == null || req.nomePasta().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Campo obrigatório",
                            "O nome da pasta não pode ser vazio",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            PastaFormulario pastaCriada = service.criarPasta(req, authentication);

            long quantidade = formularioRepository.countByPastaFormularioId(pastaCriada.getId());
            long tamanho = formularioRepository.findByPastaFormulario(pastaCriada)
                    .stream()
                    .mapToLong(Formulario::getTamanho)
                    .sum();

            PastaFormularioDTO dto = PastaFormularioDTO.fromEntity(pastaCriada, quantidade, tamanho);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

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
                            "Erro ao criar pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }


    // ✅ ENDPOINT - Renomear pasta de formulários
    @PutMapping("/pastas/{pastaId}")
    public ResponseEntity<?> renomearPasta(@PathVariable Long pastaId,
                                           @RequestBody Map<String, String> requestBody,
                                           HttpServletRequest httpRequest) {

        String novoNome = requestBody.get("novoNome");
        if (novoNome == null || novoNome.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Campo obrigatório",
                            "O novo nome não pode ser vazio",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            PastaFormulario pastaRenomeada = service.renomearPasta(pastaId, novoNome);

            long quantidade = formularioRepository.countByPastaFormularioId(pastaRenomeada.getId());
            long tamanho = formularioRepository.findByPastaFormulario(pastaRenomeada)
                    .stream()
                    .mapToLong(Formulario::getTamanho)
                    .sum();

            PastaFormularioDTO dto = PastaFormularioDTO.fromEntity(pastaRenomeada, quantidade, tamanho);

            return ResponseEntity.ok(dto);

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
                            "Nome inválido",
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
                            "Erro ao renomear pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }

    // ❌ Excluir pasta de formulários (se vazia)
    @DeleteMapping("/pastas/{pastaId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> excluirPasta(@PathVariable Long pastaId,
                                          HttpServletRequest httpRequest) {
        try {
            service.excluirPasta(pastaId);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(
                            HttpStatus.NOT_FOUND.value(),
                            "Pasta não encontrada",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Não foi possível excluir",
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
                            "Erro ao excluir pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }

    @PostMapping("/pastas/{id}/upload")
    public ResponseEntity<?> upload(@PathVariable Long id,
                                    @RequestParam("file") MultipartFile file,
                                    Authentication authentication,
                                    HttpServletRequest httpRequest) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Arquivo obrigatório",
                            "O arquivo enviado não pode ser vazio",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            UploadFormularioResponse response = service.uploadFormulario(id, file, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

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

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro de I/O",
                            "Falha ao salvar o arquivo: " + e.getMessage(),
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

    @PostMapping("/pastas/{id}/upload-multiplo")
    public ResponseEntity<?> uploadMultiplos(@PathVariable Long id,
                                             @RequestParam("files") List<MultipartFile> files,
                                             Authentication authentication,
                                             HttpServletRequest httpRequest) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Arquivos obrigatórios",
                            "É necessário enviar pelo menos um arquivo",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            List<UploadFormularioResponse> responseList = service.uploadMultiplos(id, files, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseList);

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

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro de I/O",
                            "Falha ao salvar um ou mais arquivos: " + e.getMessage(),
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

    // 🔄 ENDPOINT - Substituir formulário existente
    @PutMapping("/formularios/{formularioId}/substituir")
    public ResponseEntity<?> substituirFormulario(@PathVariable Long formularioId,
                                                  @RequestParam("file") MultipartFile novoArquivo,
                                                  HttpServletRequest httpRequest) {

        if (novoArquivo == null || novoArquivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Arquivo obrigatório",
                            "O arquivo enviado não pode ser vazio",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            UploadFormularioResponse response = service.substituirFormulario(formularioId, novoArquivo);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(
                            HttpStatus.NOT_FOUND.value(),
                            "Formulário não encontrado",
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
                            "Erro ao substituir formulário",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }


    // ❌ Excluir formulário
    @DeleteMapping("/formularios/{formularioId}")
    public ResponseEntity<?> excluir(@PathVariable Long formularioId,
                                     HttpServletRequest httpRequest) {
        try {
            service.excluirFormulario(formularioId);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(
                            HttpStatus.NOT_FOUND.value(),
                            "Formulário não encontrado",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (IllegalStateException e) {
            // Exemplo: erro ao excluir porque o arquivo físico não existe
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Não foi possível excluir",
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
                            "Erro ao excluir formulário",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }

    // ✅ ENDPOINT - Renomear formulário
    @PutMapping("/formularios/{formularioId}/renomear")
    public ResponseEntity<?> renomearFormulario(@PathVariable Long formularioId,
                                                @RequestBody Map<String, String> requestBody,
                                                HttpServletRequest httpRequest) {

        String novoNome = requestBody.get("novoNome");
        if (novoNome == null || novoNome.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Campo obrigatório",
                            "O novo nome não pode ser vazio",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            Formulario formularioRenomeado = service.renomearFormulario(formularioId, novoNome);
            FormularioDTO dto = FormularioDTO.fromEntity(formularioRenomeado);

            return ResponseEntity.ok(dto);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(
                            HttpStatus.NOT_FOUND.value(),
                            "Formulário não encontrado",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "Nome inválido",
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
                            "Erro ao renomear formulário",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }


    // ❌ Excluir varios formulários de uma vez
    @DeleteMapping("/formularios/excluir-lote")
    public ResponseEntity<?> excluirVarios(@RequestBody List<Long> ids,
                                           HttpServletRequest httpRequest) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(
                            HttpStatus.BAD_REQUEST.value(),
                            "IDs obrigatórios",
                            "É necessário informar ao menos um ID de formulário para exclusão",
                            httpRequest.getRequestURI()
                    ));
        }

        try {
            service.excluirVarios(ids);
            return ResponseEntity.noContent().build();

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
                            "Não foi possível excluir",
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
                            "Erro ao excluir formulários",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    ));
        }
    }


}