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
                .anyMatch(r -> r.getNome().equals("ADMIN"));

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
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
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


    // 🔹 Método auxiliar para mapear solicitação em DTO enxuto
    private SolicitacaoResumoDTO mapSolicitacaoResumo(Solicitacao s) {
        return new SolicitacaoResumoDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus()
        );
    }

    // ✅ Gerar lista de DTOs com filtros aplicados
    @Transactional(readOnly = true)
    public List<MotoristaRelatorioDTO> gerarRelatorio(String nome, String matricula) {
        return motoristaRepository.filtrarRelatorio(nome, matricula).stream()
                .map(m -> new MotoristaRelatorioDTO(
                        m.getId(),
                        m.getMatricula(),
                        m.getNome(),
                        m.getTelefone(),
                        m.isAtivo()
                ))
                .toList();
    }

    // ✅ Exportar Excel
    public ByteArrayInputStream exportarExcel(String nome, String matricula) throws IOException {
        List<MotoristaRelatorioDTO> motoristas = gerarRelatorio(nome, matricula);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Motoristas");

            // Cabeçalho
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Matrícula");
            header.createCell(2).setCellValue("Nome");
            header.createCell(3).setCellValue("Telefone");
            header.createCell(4).setCellValue("Ativo");

            // Dados
            int rowIdx = 1;
            for (MotoristaRelatorioDTO m : motoristas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.id());
                row.createCell(1).setCellValue(m.matricula());
                row.createCell(2).setCellValue(m.nome());
                row.createCell(3).setCellValue(m.telefone());
                row.createCell(4).setCellValue(m.ativo());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ✅ Exportar CSV
    public ByteArrayInputStream exportarCsv(String nome, String matricula) {
        List<MotoristaRelatorioDTO> motoristas = gerarRelatorio(nome, matricula);

        StringBuilder sb = new StringBuilder();
        sb.append("ID;Matrícula;Nome;Telefone;Ativo\n");

        for (MotoristaRelatorioDTO m : motoristas) {
            sb.append(m.id()).append(";")
                    .append(m.matricula()).append(";")
                    .append(m.nome()).append(";")
                    .append(m.telefone()).append(";")
                    .append(m.ativo()).append("\n");

        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    // ✅ Exportar PDF
    public ByteArrayInputStream exportarPdf(String nome, String matricula) {
        List<MotoristaRelatorioDTO> motoristas = gerarRelatorio(nome, matricula);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle pageSize = PDRectangle.A4;
            float margin = 50;
            float yStart = pageSize.getUpperRightY() - margin;
            float tableWidth = pageSize.getWidth() - 2 * margin;
            float rowHeight = 20;

            // colunas
            String[] colunas = {"ID", "Matrícula", "Nome", "Telefone","Ativo"};
            float[] colWidths = {50, 100, 200, 150,50};

            int rowIndex = 0;
            int pageNumber = 1;

            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = yStart;

            // título
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(pageSize.getWidth() / 2 - 100, yPosition);
            contentStream.showText("Relatório de Motoristas");
            contentStream.endText();

            yPosition -= 40;

            // cabeçalho
            yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);

            // linhas
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            for (MotoristaRelatorioDTO m : motoristas) {
                float nextX = margin;

                // zebra
                if (rowIndex % 2 == 0) {
                    contentStream.setNonStrokingColor(240, 240, 240);
                    contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0);
                }

                String[] valores = {
                        String.valueOf(m.id()),
                        m.matricula(),
                        m.nome(),
                        m.telefone()
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
            throw new RuntimeException("Erro ao gerar PDF de motoristas", e);
        }
    }

    // ✅ Cabeçalho
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

    // ✅ Rodapé
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
