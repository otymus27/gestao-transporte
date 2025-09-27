package br.com.carro.controllers;

import br.com.carro.autenticacao.JpaUserDetailsService;
import br.com.carro.entities.DTO.*;
import br.com.carro.entities.Pasta;
import br.com.carro.entities.Usuario.Usuario;
import br.com.carro.exceptions.ErrorMessage;
import br.com.carro.services.PastaService;
import br.com.carro.utils.AuthService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import java.nio.file.AccessDeniedException;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pastas")
public class PastaController {
    private static final Logger logger = LoggerFactory.getLogger(PastaController.class);


    @Autowired
    private PastaService pastaService;
    private AuthService authService;
    private JpaUserDetailsService userDetailsService;

    // ✅ Use constructor injection
    public PastaController( AuthService authService) {
        this.authService = authService;
    }

    // ✅ ENDPOINT 01 - Controller para criar pasta raiz ou subpastas
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> criarPasta(@RequestBody @Valid PastaRequestDTO pastaDTO,
                                        Authentication authentication,
                                        HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            Pasta novaPasta = pastaService.criarPasta(pastaDTO, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(PastaDTO.fromEntity(novaPasta));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.warn("Erro ao criar pasta: {}", e.getMessage());
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (AccessDeniedException | SecurityException e) {
            logger.warn("Acesso negado ao criar pasta");
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para criar pastas na raiz.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar pasta", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Ocorreu um erro inesperado ao criar a pasta.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ENDPOINT 02 - Lista só as pastas principais ou raiz
    @GetMapping("/raiz")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarPastasRaiz(Authentication authentication, HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            List<PastaDTO> pastas = pastaService.listarPastasRaiz(usuarioLogado)
                    .stream()
                    .map(PastaDTO::fromEntity)
                    .toList();
            return ResponseEntity.ok(pastas);
        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para listar pastas raiz.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar pastas raiz.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ENDPOINT 03 - Lista toda hierarquia de pastas
    @GetMapping("/arvore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getArvorePastas(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "nome") String ordenarPor,
            @RequestParam(required = false, defaultValue = "true") boolean ordemAsc,
            @RequestParam(required = false) String extensaoArquivo,
            @RequestParam(required = false) Long tamanhoMinArquivo,
            @RequestParam(required = false) Long tamanhoMaxArquivo,
            @RequestParam(required = false) Integer profundidadeMax,
            HttpServletRequest request
    ) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);

            // 🔹 Monta o filtro a partir dos query params
            PastaFilterDTO filtro = new PastaFilterDTO();
            filtro.setOrdenarPor(ordenarPor);
            filtro.setOrdemAsc(ordemAsc);
            filtro.setExtensaoArquivo(extensaoArquivo);
            filtro.setTamanhoMinArquivo(tamanhoMinArquivo);
            filtro.setTamanhoMaxArquivo(tamanhoMaxArquivo);
            filtro.setProfundidadeMax(profundidadeMax);

            List<PastaCompletaDTO> arvore = pastaService.getTodasPastasCompletas(usuarioLogado, filtro);
            return ResponseEntity.ok(arvore);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para visualizar a árvore de pastas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar árvore de pastas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    // ✅ ENDPOINT 04 - Método para busca de pastas e arquivos por id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> getPastaPorId(@PathVariable Long id,
                                           Authentication authentication,
                                           @ModelAttribute PastaFilterDTO filtro,
                                           HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            PastaCompletaDTO pasta = pastaService.getPastaCompletaPorId(id, usuarioLogado, filtro);
            return ResponseEntity.ok(pasta);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (AccessDeniedException | SecurityException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para acessar esta pasta.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao buscar a pasta.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    /**

        Verifica permissão do usuário (ADMIN ou GERENTE com acesso).
        Exclui recursivamente todas as subpastas e arquivos.
        Remove do filesystem e do banco de dados.
        Retorna status apropriado (200 OK, 403 Forbidden, 404 Not Found ou 500 Internal Server Error).

    */

    // ✅ ENDPOINT 05 - Exclusão de pastas por id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> excluirPasta(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            pastaService.excluirPasta(id, usuarioLogado);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | EntityNotFoundException e) {
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
                    "Você não tem permissão para excluir pasta raiz.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao excluir a pasta.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ENDPOINT 06 - Atualizar campos de pastas por id
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> atualizarPasta(@PathVariable Long id,
                                            @RequestBody PastaUpdateDTO pastaDTO,
                                            Authentication authentication,
                                            HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            Pasta pastaAtualizada = pastaService.atualizarPasta(id, pastaDTO, usuarioLogado);
            return ResponseEntity.ok(PastaDTO.fromEntity(pastaAtualizada));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
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
                    "Você não tem permissão para atualizar esta pasta.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao atualizar a pasta.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ENDPOINT 07 - Lista todas as pastas visíveis para o usuário logado
    @GetMapping("/subpastas")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','BASIC')")
    public ResponseEntity<?> listarPastasPorUsuario(Authentication authentication,
                                                    @ModelAttribute PastaFilterDTO filtro,
                                                    HttpServletRequest request) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            List<PastaCompletaDTO> pastas = pastaService.listarPastasPorUsuario(usuarioLogado, filtro);
            return ResponseEntity.ok(pastas);
        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Você não tem permissão para listar essas pastas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro ao listar pastas.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ENDPOINT 08 - Renomear pastas por id
    @PatchMapping("/{id}/renomear")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> renomearPasta(@PathVariable Long id,
                                           @RequestBody Map<String, String> request,
                                           Authentication authentication,
                                           HttpServletRequest httpRequest) {
        try {
            String novoNome = request.get("novoNome");
            if (novoNome == null || novoNome.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                "Campo obrigatório",
                                "O novo nome da pasta é obrigatório.",
                                httpRequest.getRequestURI()));
            }

            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            Pasta pastaAtualizada = pastaService.renomearPasta(id, novoNome, usuarioLogado);
            return ResponseEntity.ok(PastaDTO.fromEntity(pastaAtualizada));

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                            "Erro ao renomear pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao renomear pasta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro interno",
                            "Erro ao renomear a pasta.",
                            httpRequest.getRequestURI()));
        }
    }

    // ✅ ENDPOINT 09 - Mover pasta de um local
    @PatchMapping("/{id}/mover")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> moverPasta(@PathVariable Long id,
                                        @RequestParam(required = false) Long novaPastaPaiId,
                                        Authentication authentication,
                                        HttpServletRequest httpRequest) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            Pasta pastaMovida = pastaService.moverPasta(id, novaPastaPaiId, usuarioLogado);
            return ResponseEntity.ok(PastaDTO.fromEntity(pastaMovida));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                            "Erro ao mover pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao mover pasta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro interno",
                            "Erro ao mover a pasta.",
                            httpRequest.getRequestURI()));
        }
    }

    // ✅ ENDPOINT 10 - Copiar pasta para outra pasta
    @PostMapping("/{id}/copiar")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> copiarPasta(@PathVariable Long id,
                                         @RequestParam(required = false) Long destinoPastaId,
                                         Authentication authentication,
                                         HttpServletRequest httpRequest) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            var novaPasta = pastaService.copiarPasta(id, destinoPastaId, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(PastaDTO.fromEntity(novaPasta));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                            "Pasta não encontrada",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                            "Erro ao copiar pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao copiar pasta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro interno",
                            "Erro ao copiar a pasta.",
                            httpRequest.getRequestURI()));
        }
    }

    // ✅ ENDPOINT 11 - Excluir várias pastas
    @DeleteMapping("/excluir-lote")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> excluirPastasEmLote(@RequestBody PastaExcluirDTO pastaExcluirDTO,
                                                 Authentication authentication,
                                                 HttpServletRequest httpRequest) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            pastaService.excluirPastasEmLote(pastaExcluirDTO.idsPastas(), pastaExcluirDTO.excluirConteudo(), usuarioLogado);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                            "Pasta não encontrada",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                            "Erro ao excluir pastas",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (Exception e) {
            logger.error("Erro ao excluir pastas em lote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro interno",
                            "Erro ao excluir pastas.",
                            httpRequest.getRequestURI()));
        }
    }

    // ✅ ENDPOINT 12 - Substituir uma pasta por outra
    @PutMapping("/{idDestino}/substituir/{idOrigem}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> substituirPasta(@PathVariable Long idDestino,
                                             @PathVariable Long idOrigem,
                                             Authentication authentication,
                                             HttpServletRequest httpRequest) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            Pasta pastaSubstituida = pastaService.substituirConteudoPasta(idOrigem, idDestino, usuarioLogado);
            return ResponseEntity.ok(PastaDTO.fromEntity(pastaSubstituida));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                            "Pasta não encontrada",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorMessage(HttpStatus.FORBIDDEN.value(),
                            "Acesso negado",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro ao substituir pasta",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao substituir pasta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro interno",
                            "Erro inesperado ao substituir pasta",
                            httpRequest.getRequestURI()));
        }
    }

    // ✅ ENDPOINT 13 - Atualizar permissões de pastas
    @PostMapping("/permissao/acao")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')") // Apenas ADMIN e GERENTE podem tentar atualizar permissões
    public ResponseEntity<?> atualizarPermissoesAcao(@RequestBody PastaPermissaoAcaoDTO dto,
                                                     Authentication authentication,
                                                     HttpServletRequest httpRequest) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);

            pastaService.atualizarPermissoesAcao(
                    dto.pastaId(),
                    dto.adicionarUsuariosIds(),
                    dto.removerUsuariosIds(),
                    usuarioLogado
            );

            // ✅ Resposta padronizada em JSON
            PermissaoDTO sucesso = new PermissaoDTO(
                    HttpStatus.OK.value(),
                    "Permissões atualizadas com sucesso",
                    "As permissões foram aplicadas à pasta de forma correta.",
                    httpRequest.getRequestURI(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(sucesso);

        } catch (EntityNotFoundException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.NOT_FOUND.value(),
                    "Pasta não encontrada",
                    e.getMessage(),
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (AccessDeniedException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    e.getMessage(), // Mensagem já vem clara do service
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (IllegalArgumentException e) {
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    "Dados inválidos",
                    e.getMessage(),
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar permissões", e);
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Erro interno no servidor",
                    "Erro inesperado ao atualizar permissões da pasta.",
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    // ✅ ENDPOINT 14 - Listar usuários por pasta
    @GetMapping("/{id}/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<?> listarUsuariosPorPasta(@PathVariable Long id,
                                                    Authentication authentication,
                                                    HttpServletRequest httpRequest) {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado(authentication);
            List<UsuarioResumoDTO> usuarios = pastaService.listarUsuariosPorPasta(id, usuarioLogado)
                    .stream()
                    .map(UsuarioResumoDTO::fromEntity)
                    .toList();
            return ResponseEntity.ok(usuarios);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                            "Pasta não encontrada",
                            e.getMessage(),
                            httpRequest.getRequestURI()));
        } catch (Exception e) {
            logger.error("Erro ao listar usuários da pasta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erro interno",
                            "Erro ao buscar usuários da pasta",
                            httpRequest.getRequestURI()));
        }
    }




}