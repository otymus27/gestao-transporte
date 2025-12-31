package com.br.sistema.services;

import com.br.sistema.entities.Motorista.DTO.MotoristaRelatorioDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaRequestDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDetalhadoDTO;
import com.br.sistema.entities.Motorista.Motorista;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.MotoristaRepository;
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
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;

    public MotoristaService(MotoristaRepository motoristaRepository) {
        this.motoristaRepository = motoristaRepository;
    }

    //Listagem simples com paginação
    @Transactional(readOnly = true)
    public Page<MotoristaDetalhadoDTO> listarTodosPaginado(Pageable pageable) {
        return motoristaRepository.findAll(pageable)
                .map(m -> MotoristaDetalhadoDTO.fromEntity(m, false)); // 🚫 sem solicitações
    }

    // ✅ Buscar um motorista por id
    @Transactional(readOnly = true)
    public MotoristaDetalhadoDTO buscarPorId(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));
        return MotoristaDetalhadoDTO.fromEntity(motorista, false); // 🚫 não traz solicitações
    }



    // ✅ Cadastro com validações e controle de permissão
    @Transactional(noRollbackFor = EntityExistsException.class)
    public MotoristaDetalhadoDTO cadastrar(
            MotoristaRequestDTO dto,
            Usuario usuarioLogado
    ) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.matricula() == null || dto.matricula().isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (dto.telefone() == null || dto.telefone().isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar motoristas)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> r.getNome().equals("ADMIN") || r.getNome().equals("GERENTE"));

        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar motoristas.");
        }

        // 4️⃣ Validar duplicidade
        if (motoristaRepository.existsByMatricula(dto.matricula())) {
            throw new EntityExistsException("Já existe motorista com esta matrícula.");
        }

        // 5️⃣ Criar entidade Motorista
        Motorista motorista = new Motorista();
        motorista.setMatricula(dto.matricula().trim());
        motorista.setNome(dto.nome().trim());
        motorista.setTelefone(dto.telefone().trim());

        try {
            motoristaRepository.save(motorista);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: motorista já existe.");
        }

        // ✅ Agora converte a entidade para DTO
        return MotoristaDetalhadoDTO.fromEntity(motorista,false);
    }



    // ✅ Atualizar motorista (inclui matrícula também)
    @Transactional
    public MotoristaDetalhadoDTO atualizar(Long id, MotoristaRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // Apenas ADMIN pode atualizar
        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(r -> r.getNome().equals("ADMIN") || r.getNome().equals("GERENTE"));
        if (!isAdmin) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar motoristas.");
        }

        // Validações básicas
        if (dto.matricula() == null || dto.matricula().isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatória.");
        }
        if (dto.telefone() == null || dto.telefone().isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório.");
        }

        // Busca motorista existente
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));

        // Atualiza dados
        motorista.setMatricula(dto.matricula().trim());
        motorista.setNome(dto.nome().trim());
        motorista.setTelefone(dto.telefone().trim());
        motorista.setAtivo(dto.ativo());

        motoristaRepository.save(motorista);


        motoristaRepository.save(motorista);

        // Retorna DTO sem solicitações
        return MotoristaDetalhadoDTO.fromEntity(motorista, false);
    }

    // ✅ Exclusão com checagem de permissão e tratamento de FK
    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Verificar permissões (somente ADMIN pode excluir motoristas)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para excluir motoristas.");
        }

        // 3️⃣ Validar existência
        var motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado com id " + id));

        // 4️⃣ Excluir com tratamento de integridade (FK em solicitações, se houver)
        try {
            motoristaRepository.delete(motorista);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há solicitações vinculadas a este motorista.");
        }
    }



    // ✅ Buscar todos (resumido: sem lista de solicitações)
    @Transactional(readOnly = true)
    public List<MotoristaDetalhadoDTO> listarTodosResumido() {
        return motoristaRepository.findAll().stream()
                .map(m -> new MotoristaDetalhadoDTO(
                        m.getId(),
                        m.getMatricula(),
                        m.getNome(),
                        m.getTelefone(),
                        m.isAtivo(),
                        List.of() // vazio para não pesar na listagem
                ))
                .toList();
    }



    // ✅ Buscar detalhado (motorista + solicitações atendidas)
    @Transactional(readOnly = true)
    public MotoristaDetalhadoDTO buscarDetalhado(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));
        return MotoristaDetalhadoDTO.fromEntity(motorista, true); // ✅ inclui solicitações
    }

    @Transactional(readOnly = true)
    public Page<MotoristaDetalhadoDTO> filtrar(String nome, String matricula, Pageable pageable) {
        return motoristaRepository.filtrar(nome, matricula, pageable)
                .map(m -> MotoristaDetalhadoDTO.fromEntity(m, false)); // 🚫 sem solicitações
    }

    @Transactional(readOnly = true)
    public Page<MotoristaDetalhadoDTO> filtrarPorTermo(String termo, Pageable pageable) {

        if (termo == null || termo.isBlank()) {
            // se não mandou nada, reaproveita o listar padrão se você tiver
            return motoristaRepository.findAll(pageable)
                    .map(m -> MotoristaDetalhadoDTO.fromEntity(m, false));
        }

        String filtro = termo.trim();

        String nome = null;
        String matricula = null;

        // Regra simples:
        // - só dígitos → considera matrícula
        // - qualquer outra coisa → considera nome (parte do nome)
        if (filtro.matches("\\d+")) {
            matricula = filtro;
        } else {
            nome = filtro;
        }

        return motoristaRepository.filtrar(nome, matricula, pageable)
                .map(m -> MotoristaDetalhadoDTO.fromEntity(m, false)); // 🚫 sem solicitações
    }


    // 🔹 Método auxiliar para mapear solicitação em DTO enxuto
    private SolicitacaoResumoDTO mapSolicitacaoResumo(Solicitacao s) {
        return new SolicitacaoResumoDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus()
        );
    }



}
