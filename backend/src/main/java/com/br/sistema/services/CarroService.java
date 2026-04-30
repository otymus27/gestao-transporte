package com.br.sistema.services;

import com.br.sistema.entities.Carro.Carro;
import com.br.sistema.entities.Carro.DTO.CarroDetalhadoDTO;
import com.br.sistema.entities.Carro.DTO.CarroRelatorioDTO;
import com.br.sistema.entities.Carro.DTO.CarroRequestDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.CarroRepository;

import com.itextpdf.text.Document;
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
public class CarroService {

    @Autowired
    private final CarroRepository carroRepository;

    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    // ✅ Cadastro com validações e controle de permissão (padrão Motorista)
    @Transactional(noRollbackFor = EntityExistsException.class)
    public CarroDetalhadoDTO cadastrar(CarroRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.marca() == null || dto.marca().isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória.");
        }
        if (dto.modelo() == null || dto.modelo().isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório.");
        }
        if (dto.tipo() == null || dto.tipo().isBlank()) {
            throw new IllegalArgumentException("Tipo é obrigatório.");
        }
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()) || "SUPERVISOR".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar carros.");
        }

        // 4️⃣ Validar duplicidade de placa
        if (carroRepository.existsByPlaca(dto.placa().trim())) {
            throw new EntityExistsException("Já existe carro com esta placa.");
        }

        // 5️⃣ Criar entidade Carro
        Carro carro = new Carro();
        carro.setMarca(dto.marca().trim());
        carro.setModelo(dto.modelo().trim());
        carro.setTipo(dto.tipo().trim());
        carro.setPlaca(dto.placa().trim());

        try {
            carroRepository.save(carro);
        } catch (DataIntegrityViolationException e) {
            // (unique constraint em placa, por exemplo)
            throw new EntityExistsException("Erro ao salvar: carro já existe.");
        }

        // ✅ Converte entidade para DTO (sem solicitações no cadastro)
        return CarroDetalhadoDTO.fromEntity(carro, false);
    }

    // ✅ Atualização com mesmas regras de permissão/validação
    @Transactional(noRollbackFor = EntityExistsException.class)
    public CarroDetalhadoDTO atualizar(Long id, CarroRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.marca() == null || dto.marca().isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória.");
        }
        if (dto.modelo() == null || dto.modelo().isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório.");
        }
        if (dto.tipo() == null || dto.tipo().isBlank()) {
            throw new IllegalArgumentException("Tipo é obrigatório.");
        }
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode atualizar carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()) || "SUPERVISOR".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar carros.");
        }

        // 4️⃣ Buscar entidade
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com id " + id));

        // 5️⃣ Checar duplicidade de placa (ignora o próprio carro)
        carroRepository.findByPlaca(dto.placa().trim())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> { throw new EntityExistsException("Já existe outro carro com esta placa."); });

        // 6️⃣ Aplicar alterações
        carro.setMarca(dto.marca().trim());
        carro.setModelo(dto.modelo().trim());
        carro.setTipo(dto.tipo().trim());
        carro.setPlaca(dto.placa().trim());

        try {
            carroRepository.save(carro);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao atualizar: placa já em uso.");
        }

        return CarroDetalhadoDTO.fromEntity(carro, false);
    }

    // ✅ Listagem paginada (sem solicitações para ficar leve) com filtro para gerar relatorio
    @Transactional(readOnly = true)
    public Page<CarroDetalhadoDTO> listar(String filtro,Pageable pageable) {
        return carroRepository.filtrar(filtro,pageable)
                .map(c -> CarroDetalhadoDTO.fromEntity(c, false));
    }

    // ✅ Detalhe por ID (permite controlar se inclui solicitações)
    @Transactional(readOnly = true)
    public CarroDetalhadoDTO buscarPorId(Long id, boolean incluirSolicitacoes) {
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com id " + id));

        return CarroDetalhadoDTO.fromEntity(carro, incluirSolicitacoes);
    }

    // ✅ Busca por placa (útil para telas/relatórios)
    @Transactional(readOnly = true)
    public CarroDetalhadoDTO buscarPorPlaca(String placa, boolean incluirSolicitacoes) {
        Carro carro = carroRepository.findByPlaca(placa.trim())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com placa " + placa));
        return CarroDetalhadoDTO.fromEntity(carro, incluirSolicitacoes);
    }

    // ✅ Exclusão com checagem de permissão e tratamento de FK
    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Verificar permissões (somente ADMIN pode excluir carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para excluir carros.");
        }

        // 3️⃣ Validar existência ANTES de excluir
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com id " + id));

        // 4️⃣ Excluir com tratamento de integridade (FK em solicitações)
        try {
            carroRepository.delete(carro); // ✅ agora deletamos a entidade já carregada
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há solicitações vinculadas a este carro.");
        }
    }

    @Transactional(readOnly = true)
    public Page<CarroDetalhadoDTO> filtrar(String placa, String marca, String modelo, Pageable pageable) {
        return carroRepository.filtrar(placa, marca, modelo, pageable)
                .map(c -> CarroDetalhadoDTO.fromEntity(c, false)); // 🚘 DTO simples para carro
    }


}
