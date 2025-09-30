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
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode atualizar carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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

    @Transactional(readOnly = true)
    public List<CarroRelatorioDTO> gerarRelatorio(String filtro) {
        return carroRepository.filtrarSemPaginacao(filtro).stream()
                .map(c -> new CarroRelatorioDTO(
                        c.getId(),
                        c.getMarca(),
                        c.getModelo(),
                        c.getPlaca()
                ))
                .toList();
    }

    // Métodos auxiliares
    // ✅ Exportar Excel
    public ByteArrayInputStream exportarExcel(String filtro) throws IOException {
        List<CarroRelatorioDTO> carros = gerarRelatorio(filtro);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Carros");

            // Cabeçalho
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Marca");
            header.createCell(2).setCellValue("Modelo");
            header.createCell(3).setCellValue("Placa");

            // Dados
            int rowIdx = 1;
            for (CarroRelatorioDTO c : carros) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.id());
                row.createCell(1).setCellValue(c.marca());
                row.createCell(2).setCellValue(c.modelo());
                row.createCell(3).setCellValue(c.placa());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ✅ Exportar CSV
    public ByteArrayInputStream exportarCsv(String filtro) {
        List<CarroRelatorioDTO> carros = gerarRelatorio(filtro);

        StringBuilder sb = new StringBuilder();
        sb.append("ID;Marca;Modelo;Placa\n");

        for (CarroRelatorioDTO c : carros) {
            sb.append(c.id()).append(";")
                    .append(c.marca()).append(";")
                    .append(c.modelo()).append(";")
                    .append(c.placa()).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    public ByteArrayInputStream exportarPdf(String filtro) {
        List<CarroRelatorioDTO> carros = gerarRelatorio(filtro);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle pageSize = PDRectangle.A4;
            float margin = 50;
            float yStart = pageSize.getUpperRightY() - margin;
            float tableWidth = pageSize.getWidth() - 2 * margin;
            float rowHeight = 20;

            // colunas
            String[] colunas = {"ID", "Marca", "Modelo", "Placa"};
            float[] colWidths = {50, 150, 150, 150};

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
            contentStream.showText("Relatório de Carros");
            contentStream.endText();

            yPosition -= 40;

            // cabeçalho
            yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);

            // linhas
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            for (CarroRelatorioDTO c : carros) {
                float nextX = margin;

                // zebra
                if (rowIndex % 2 == 0) {
                    contentStream.setNonStrokingColor(240, 240, 240);
                    contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0);
                }

                String[] valores = {
                        String.valueOf(c.id()),
                        c.marca(),
                        c.modelo(),
                        c.placa()
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
        for (String col : colunas) {
            cs.beginText();
            cs.newLineAtOffset(nextX + 5, yPosition - 15);
            cs.showText(col);
            cs.endText();
            nextX += colWidths[colunas.length - 1 == 0 ? 0 : colunas.length - 1];
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
