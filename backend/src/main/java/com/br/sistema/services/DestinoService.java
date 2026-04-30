package com.br.sistema.services;

import com.br.sistema.entities.Carro.DTO.CarroDetalhadoDTO;
import com.br.sistema.entities.Destino.DTO.DestinoRelatorioDTO;
import com.br.sistema.entities.Destino.Destino;
import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Destino.DTO.DestinoRequestDTO;
import com.br.sistema.entities.Destino.DTO.DestinoResponseDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.DestinoRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class DestinoService {

    @Autowired
    private final DestinoRepository destinoRepository;

    public DestinoService(DestinoRepository destinoRepository) {
        this.destinoRepository = destinoRepository;
    }

    // ✅ Cadastro com validações e controle de permissão
    @Transactional(noRollbackFor = EntityExistsException.class)
    public DestinoResponseDTO cadastrar(DestinoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do destino é obrigatório.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar destino)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()) || "SUPERVISOR".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar destino.");
        }

        // 4️⃣ Validar duplicidade
        if (destinoRepository.findByNome(dto.nome().trim()).isPresent()) {
            throw new EntityExistsException("Já existe um destino com este nome.");
        }

        // 5️⃣ Criar entidade
        Destino destino = new Destino();
        destino.setNome(dto.nome().trim());

        try {
            destinoRepository.save(destino);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: destino já existe.");
        }

        return new DestinoResponseDTO(destino.getId(), destino.getNome());
    }

    // ✅ Atualização
    @Transactional(noRollbackFor = EntityExistsException.class)
    public DestinoResponseDTO atualizar(Long id, DestinoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do destino é obrigatório.");
        }

        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()) || "SUPERVISOR".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar destino.");
        }

        Destino destino = destinoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado com id " + id));

        destinoRepository.findByNome(dto.nome().trim())
                .filter(d -> !d.getId().equals(id))
                .ifPresent(d -> { throw new EntityExistsException("Já existe outro destino com este nome."); });

        destino.setNome(dto.nome().trim());

        try {
            destinoRepository.save(destino);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao atualizar: nome já em uso.");
        }

        return new DestinoResponseDTO(destino.getId(), destino.getNome());
    }

    // ✅ Listagem paginada com ordenação de campos
    @Transactional(readOnly = true)
    public Page<DestinoResponseDTO> listar(Pageable pageable) {
        return destinoRepository.findAll(pageable)
                .map(d -> new DestinoResponseDTO(d.getId(), d.getNome()));
    }

    // ✅ Filtro por nome
    @Transactional(readOnly = true)
    public Page<DestinoDetalhadoDTO> filtrar(String nome, Pageable pageable) {
        return destinoRepository.filtrar(nome, pageable)
                .map(d -> DestinoDetalhadoDTO.fromEntity(d, false)); // 🚘 DTO simples para carro
    }

    // ✅ Detalhe por ID (pode incluir solicitações)
    @Transactional(readOnly = true)
    public DestinoDetalhadoDTO buscarPorId(Long id, boolean incluirSolicitacoes) {
        Destino destino = destinoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado com id " + id));

        return DestinoDetalhadoDTO.fromEntity(destino, incluirSolicitacoes);
    }

    // ✅ Exclusão
    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para excluir destino.");
        }

        Destino destino = destinoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado com id " + id));

        try {
            destinoRepository.delete(destino);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há solicitações vinculadas a este destino.");
        }
    }

}
