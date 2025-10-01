package com.br.sistema.services;

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
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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
    public Page<SetorResponseDTO> filtrarPorNome(String nome, Pageable pageable) {
        return setorRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable)
                .map(s -> new SetorResponseDTO(s.getId(), s.getNome()));
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

    // ✅ Gerar lista de DTOs
    @Transactional(readOnly = true)
    public List<SetorRelatorioDTO> gerarRelatorio(String filtro) {
        return setorRepository.filtrarSemPaginacao(filtro).stream()
                .map(s -> new SetorRelatorioDTO(
                        s.getId(),
                        s.getNome()
                ))
                .toList();
    }

    // ✅ Exportar Excel
    public ByteArrayInputStream exportarExcel(String filtro) throws IOException {
        List<SetorRelatorioDTO> setores = gerarRelatorio(filtro);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Setores");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nome");

            int rowIdx = 1;
            for (SetorRelatorioDTO s : setores) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.id());
                row.createCell(1).setCellValue(s.nome());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ✅ Exportar CSV
    public ByteArrayInputStream exportarCsv(String filtro) {
        List<SetorRelatorioDTO> setores = gerarRelatorio(filtro);

        StringBuilder sb = new StringBuilder();
        sb.append("ID;Nome\n");

        for (SetorRelatorioDTO s : setores) {
            sb.append(s.id()).append(";")
                    .append(s.nome()).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    // ✅ Exportar PDF
    public ByteArrayInputStream exportarPdf(String filtro) {
        List<SetorRelatorioDTO> setores = gerarRelatorio(filtro);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle pageSize = PDRectangle.A4;
            float margin = 50;
            float yStart = pageSize.getUpperRightY() - margin;
            float tableWidth = pageSize.getWidth() - 2 * margin;
            float rowHeight = 20;

            String[] colunas = {"ID", "Nome"};
            float[] colWidths = {50, 400};

            int rowIndex = 0;
            int pageNumber = 1;

            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = yStart;

            // título
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(pageSize.getWidth() / 2 - 80, yPosition);
            contentStream.showText("Relatório de Setores");
            contentStream.endText();

            yPosition -= 40;
            yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);

            // linhas
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            for (SetorRelatorioDTO s : setores) {
                float nextX = margin;

                if (rowIndex % 2 == 0) {
                    contentStream.setNonStrokingColor(240, 240, 240);
                    contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0);
                }

                String[] valores = {String.valueOf(s.id()), s.nome()};

                for (int i = 0; i < valores.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(nextX + 5, yPosition - 15);
                    contentStream.showText(valores[i] != null ? valores[i] : "");
                    contentStream.endText();
                    nextX += colWidths[i];
                }

                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.stroke();

                yPosition -= rowHeight;
                rowIndex++;

                if (yPosition <= margin + 40) {
                    desenharRodape(contentStream, pageSize, margin, pageNumber++);
                    contentStream.close();

                    page = new PDPage(pageSize);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;

                    yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                }
            }

            desenharRodape(contentStream, pageSize, margin, pageNumber);
            contentStream.close();

            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF de setores", e);
        }
    }

    // ✅ Auxiliar: cabeçalho
    private float desenharCabecalho(PDPageContentStream cs, float margin, float yPosition,
                                    float tableWidth, float rowHeight, String[] colunas, float[] colWidths) throws IOException {
        cs.setNonStrokingColor(200, 200, 200);
        cs.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0);

        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        float nextX = margin;
        for (int i = 0; i < colunas.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(nextX + 5, yPosition - 15);
            cs.showText(colunas[i]);
            cs.endText();
            nextX += colWidths[i];
        }

        cs.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        cs.stroke();

        return yPosition - rowHeight;
    }

    // ✅ Auxiliar: rodapé
    private void desenharRodape(PDPageContentStream cs, PDRectangle pageSize, float margin, int pageNumber) throws IOException {
        cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);

        String dataHora = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        cs.beginText();
        cs.newLineAtOffset(margin, margin - 20);
        cs.showText("Gerado em: " + dataHora);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(pageSize.getWidth() - margin - 60, margin - 20);
        cs.showText("Página " + pageNumber);
        cs.endText();
    }


}
