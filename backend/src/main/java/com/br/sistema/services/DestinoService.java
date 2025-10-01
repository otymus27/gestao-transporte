package com.br.sistema.services;

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
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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
    public Page<DestinoResponseDTO> filtrarPorNome(String nome, Pageable pageable) {
        return destinoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable)
                .map(d -> new DestinoResponseDTO(d.getId(), d.getNome()));
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

    // ✅ Gerar lista de DTOs com filtros aplicados
    @Transactional(readOnly = true)
    public List<DestinoRelatorioDTO> gerarRelatorio(String filtro) {
        return destinoRepository.filtrarSemPaginacao(filtro).stream()
                .map(d -> new DestinoRelatorioDTO(
                        d.getId(),
                        d.getNome()
                ))
                .toList();
    }

    // ✅ Exportar Excel
    public ByteArrayInputStream exportarExcel(String filtro) throws IOException {
        List<DestinoRelatorioDTO> destinos = gerarRelatorio(filtro);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Destinos");

            // Cabeçalho
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nome");

            // Dados
            int rowIdx = 1;
            for (DestinoRelatorioDTO d : destinos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.id());
                row.createCell(1).setCellValue(d.nome());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ✅ Exportar CSV
    public ByteArrayInputStream exportarCsv(String filtro) {
        List<DestinoRelatorioDTO> destinos = gerarRelatorio(filtro);

        StringBuilder sb = new StringBuilder();
        sb.append("ID;Nome\n");

        for (DestinoRelatorioDTO d : destinos) {
            sb.append(d.id()).append(";")
                    .append(d.nome()).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    // ✅ Exportar PDF
    public ByteArrayInputStream exportarPdf(String filtro) {
        List<DestinoRelatorioDTO> destinos = gerarRelatorio(filtro);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle pageSize = PDRectangle.A4;
            float margin = 50;
            float yStart = pageSize.getUpperRightY() - margin;
            float tableWidth = pageSize.getWidth() - 2 * margin;
            float rowHeight = 20;

            // colunas
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
            contentStream.showText("Relatório de Destinos");
            contentStream.endText();

            yPosition -= 40;

            // cabeçalho
            yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);

            // linhas
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            for (DestinoRelatorioDTO d : destinos) {
                float nextX = margin;

                // zebra
                if (rowIndex % 2 == 0) {
                    contentStream.setNonStrokingColor(240, 240, 240);
                    contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0);
                }

                String[] valores = {
                        String.valueOf(d.id()),
                        d.nome()
                };

                for (int i = 0; i < valores.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(nextX + 5, yPosition - 15);
                    contentStream.showText(valores[i] != null ? valores[i] : "");
                    contentStream.endText();
                    nextX += colWidths[i];
                }

                // borda
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.stroke();

                yPosition -= rowHeight;
                rowIndex++;

                // quebra de página
                if (yPosition <= margin + 40) {
                    // rodapé antes de trocar de página
                    desenharRodape(contentStream, pageSize, margin, pageNumber++);

                    contentStream.close();
                    page = new PDPage(pageSize);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;

                    // novo cabeçalho
                    yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                }
            }

            // rodapé da última página
            desenharRodape(contentStream, pageSize, margin, pageNumber);
            contentStream.close();

            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF com PDFBox", e);
        }
    }

    // ✅ método auxiliar: cabeçalho
    private float desenharCabecalho(PDPageContentStream cs, float margin, float yPosition,
                                    float tableWidth, float rowHeight, String[] colunas, float[] colWidths) throws IOException {
        // fundo cinza
        cs.setNonStrokingColor(200, 200, 200);
        cs.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0);

        // texto
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        float nextX = margin;
        for (int i = 0; i < colunas.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(nextX + 5, yPosition - 15);
            cs.showText(colunas[i]);
            cs.endText();
            nextX += colWidths[i];
        }

        // borda
        cs.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        cs.stroke();

        return yPosition - rowHeight;
    }

    // ✅ método auxiliar: rodapé
    private void desenharRodape(PDPageContentStream cs, PDRectangle pageSize, float margin, int pageNumber) throws IOException {
        cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);

        String dataHora = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // esquerda: data/hora
        cs.beginText();
        cs.newLineAtOffset(margin, margin - 20);
        cs.showText("Gerado em: " + dataHora);
        cs.endText();

        // direita: número da página
        cs.beginText();
        cs.newLineAtOffset(pageSize.getWidth() - margin - 60, margin - 20);
        cs.showText("Página " + pageNumber);
        cs.endText();
    }
}
