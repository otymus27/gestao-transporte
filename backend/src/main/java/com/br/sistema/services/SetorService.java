package com.br.sistema.services;

import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO;
import com.br.sistema.entities.Setor.Setor;
import com.br.sistema.entities.Setor.DTO.SetorDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SetorRequestDTO;
import com.br.sistema.entities.Setor.DTO.SetorResponseDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.SetorRepository;
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
public class SetorService {

    @Autowired
    private final SetorRepository setorRepository;

    public SetorService(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    // ✅ Cadastro com validações e controle de permissão
    @Transactional(noRollbackFor = EntityExistsException.class)
    public SetorResponseDTO cadastrar(SetorRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do setor é obrigatório.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar setor)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()) || "SUPERVISOR".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar setor.");
        }

        // 4️⃣ Validar duplicidade
        if (setorRepository.findByNome(dto.nome().trim()).isPresent()) {
            throw new EntityExistsException("Já existe um setor com este nome.");
        }

        // 5️⃣ Criar entidade
        Setor setor = new Setor();
        setor.setNome(dto.nome().trim());

        try {
            setorRepository.save(setor);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: setor já existe.");
        }

        return new SetorResponseDTO(setor.getId(), setor.getNome());
    }

    // ✅ Atualização
    @Transactional(noRollbackFor = EntityExistsException.class)
    public SetorResponseDTO atualizar(Long id, SetorRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do setor é obrigatório.");
        }

        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()) || "SUPERVISOR".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar setor.");
        }

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado com id " + id));

        setorRepository.findByNome(dto.nome().trim())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> { throw new EntityExistsException("Já existe outro setor com este nome."); });

        setor.setNome(dto.nome().trim());

        try {
            setorRepository.save(setor);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao atualizar: nome já em uso.");
        }

        return new SetorResponseDTO(setor.getId(), setor.getNome());
    }

    // ✅ Listagem paginada
    @Transactional(readOnly = true)
    public Page<SetorResponseDTO> listar(Pageable pageable) {
        return setorRepository.findAll(pageable)
                .map(s -> new SetorResponseDTO(s.getId(), s.getNome()));
    }

    // ✅ Filtro por nome
    @Transactional(readOnly = true)
    public Page<SetorDetalhadoDTO> filtrar(String nome, Pageable pageable) {
        return setorRepository.filtrar(nome, pageable)
                .map(s -> SetorDetalhadoDTO.fromEntity(s, false)); // 🚘 DTO simples para carro
    }


    // ✅ Detalhe por ID (pode incluir solicitações)
    @Transactional(readOnly = true)
    public SetorDetalhadoDTO buscarPorId(Long id, boolean incluirSolicitacoes) {
        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado com id " + id));

        return SetorDetalhadoDTO.fromEntity(setor, incluirSolicitacoes);
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
            throw new AccessDeniedException("Usuário não tem permissão para excluir setor.");
        }

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado com id " + id));

        try {
            setorRepository.delete(setor);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há solicitações vinculadas a este setor.");
        }
    }


}
