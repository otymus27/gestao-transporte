package com.br.sistema.services;

import com.br.sistema.entities.Carro.DTO.CarroDetalhadoDTO;
import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;
import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO;
import com.br.sistema.entities.Setor.DTO.SetorDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO;
import com.br.sistema.entities.Solicitacao.DTO.*;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO;
import com.br.sistema.entities.Usuario.DTO.UsuarioDetalhadoDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.*;

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
import java.time.LocalDate;
import java.util.List;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CarroRepository carroRepository;
    private final MotoristaRepository motoristaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SetorRepository setorRepository;
    private final DestinoRepository destinoRepository;

    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            CarroRepository carroRepository,
            MotoristaRepository motoristaRepository,
            UsuarioRepository usuarioRepository,
            SetorRepository setorRepository,
            DestinoRepository destinoRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.carroRepository = carroRepository;
        this.motoristaRepository = motoristaRepository;
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
        this.destinoRepository = destinoRepository;
    }

    // ✅ Listagem paginada (resumida)
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listarTodosPaginado(Pageable pageable) {
        return solicitacaoRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Buscar por ID (resumido)
    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
        return toResponseDTO(solicitacao);
    }

    // ✅ Buscar detalhado
    @Transactional(readOnly = true)
    public SolicitacaoDetalhadaDTO buscarDetalhado(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
        return toDetalhadaDTO(solicitacao);
    }

    // ✅ Cadastro
    @Transactional(noRollbackFor = EntityExistsException.class)
    public SolicitacaoResponseDTO cadastrar(SolicitacaoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 🔐 Apenas ADMIN e GERENTE podem cadastrar solicitações
        boolean permitido = usuarioLogado.getRoles().stream()
                .anyMatch(r -> r.getNome().equals("ADMIN") || r.getNome().equals("GERENTE"));
        if (!permitido) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar solicitações.");
        }

        // Criar entidade
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDataSolicitacao(dto.dataSolicitacao());
        solicitacao.setStatus(dto.status());

        solicitacao.setCarro(carroRepository.findById(dto.carroId())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado.")));
        solicitacao.setMotorista(motoristaRepository.findById(dto.motoristaId())
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado.")));
        solicitacao.setUsuario(usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado.")));
        solicitacao.setSetor(setorRepository.findById(dto.setorId())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado.")));
        solicitacao.setDestino(destinoRepository.findById(dto.destinoId())
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado.")));

        solicitacao.setKmInicial(dto.kmInicial());
        solicitacao.setKmFinal(dto.kmFinal());
        solicitacao.setHoraSaida(dto.horaSaida());
        solicitacao.setHoraChegada(dto.horaChegada());

        try {
            solicitacaoRepository.save(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: solicitação já existe.");
        }

        return toResponseDTO(solicitacao);
    }

    // ✅ Atualização
    @Transactional
    public SolicitacaoResponseDTO atualizar(Long id, SolicitacaoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // Apenas ADMIN pode atualizar
        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!isAdmin) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar solicitações.");
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));

        solicitacao.setDataSolicitacao(dto.dataSolicitacao());
        solicitacao.setStatus(dto.status());
        solicitacao.setCarro(carroRepository.findById(dto.carroId())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado.")));
        solicitacao.setMotorista(motoristaRepository.findById(dto.motoristaId())
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado.")));
        solicitacao.setUsuario(usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado.")));
        solicitacao.setSetor(setorRepository.findById(dto.setorId())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado.")));
        solicitacao.setDestino(destinoRepository.findById(dto.destinoId())
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado.")));
        solicitacao.setKmInicial(dto.kmInicial());
        solicitacao.setKmFinal(dto.kmFinal());
        solicitacao.setHoraSaida(dto.horaSaida());
        solicitacao.setHoraChegada(dto.horaChegada());

        solicitacaoRepository.save(solicitacao);

        return toResponseDTO(solicitacao);
    }

    // ✅ Exclusão
    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!isAdmin) {
            throw new AccessDeniedException("Usuário não tem permissão para excluir solicitações.");
        }

        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));

        try {
            solicitacaoRepository.delete(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há vínculos de integridade.");
        }
    }

    // 🔹 Mapear entidade → DTOs
    private SolicitacaoResponseDTO toResponseDTO(Solicitacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),

                s.getCarro().getId(),
                s.getCarro().getPlaca(),
                s.getCarro().getModelo(),

                s.getMotorista().getId(),
                s.getMotorista().getNome(),

                s.getUsuario().getId(),
                s.getUsuario().getNome(),
                s.getUsuario().getUsername(),

                s.getSetor().getId(),
                s.getSetor().getNome(),


                s.getDestino().getId(),
                s.getDestino().getNome(),

                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }

    private SolicitacaoDetalhadaDTO toDetalhadaDTO(Solicitacao s) {
        return new SolicitacaoDetalhadaDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                CarroDetalhadoDTO.fromEntity(s.getCarro(),false),
                MotoristaDetalhadoDTO.fromEntity(s.getMotorista(), false),
                UsuarioDetalhadoDTO.fromEntity(s.getUsuario(), false),
                SetorDetalhadoDTO.fromEntity(s.getSetor(),false),
                DestinoDetalhadoDTO.fromEntity(s.getDestino(), false),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }

    // ✅ Filtrar por status
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorStatus(String status, Pageable pageable) {
        return solicitacaoRepository.findByStatus(status, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por motorista
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorMotorista(Long motoristaId, Pageable pageable) {
        return solicitacaoRepository.findByMotorista_Id(motoristaId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por carro
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorCarro(Long carroId, Pageable pageable) {
        return solicitacaoRepository.findByCarro_Id(carroId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por setor
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorSetor(Long setorId, Pageable pageable) {
        return solicitacaoRepository.findBySetor_Id(setorId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por usuário
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorUsuario(Long usuarioId, Pageable pageable) {
        return solicitacaoRepository.findByUsuario_Id(usuarioId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por destino
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorDestino(Long destinoId, Pageable pageable) {
        return solicitacaoRepository.findByDestino_Id(destinoId, pageable)
                .map(this::toResponseDTO);
    }


    // ✅ Filtro genérico por múltiplos parâmetros
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarGenerico(
            Long id,
            String status,
            Long motoristaId,
            Long carroId,
            Long setorId,
            String username,
            Long destinoId,
            Pageable pageable
    ) {

        // Caso só um filtro seja usado, direcionamos pro repository certo
        if (id != null) {
            return solicitacaoRepository.findById(id, pageable).map(this::toResponseDTO);
        }
        if (status != null) {
            return solicitacaoRepository.findByStatus(status, pageable).map(this::toResponseDTO);
        }
        if (motoristaId != null) {
            return solicitacaoRepository.findByMotorista_Id(motoristaId, pageable).map(this::toResponseDTO);
        }
        if (carroId != null) {
            return solicitacaoRepository.findByCarro_Id(carroId, pageable).map(this::toResponseDTO);
        }
        if (setorId != null) {
            return solicitacaoRepository.findBySetor_Id(setorId, pageable).map(this::toResponseDTO);
        }
        if (username != null) {
            return solicitacaoRepository.findByUsernameContainingIgnoreCase(username, pageable).map(this::toResponseDTO);
        }

        if (destinoId != null) {
            return solicitacaoRepository.findByDestino_Id(destinoId, pageable).map(this::toResponseDTO);
        }

        // Se nenhum filtro informado, retorna tudo paginado
        return solicitacaoRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public List<SolicitacaoPorDiaDTO> buscarPorIntervalo(LocalDate inicio, LocalDate fim) {
        return solicitacaoRepository.buscarPorDatas(inicio, fim);
    }

    public List<SolicitacaoPorMotoristaDTO> buscarPorMotorista() {
        return solicitacaoRepository.buscarPorMotorista();
    }

    public List<SolicitacaoPorSetorDTO> buscarPorSetor() {
        return solicitacaoRepository.buscarPorSetor();
    }

    public List<SolicitacaoPorUsuarioDTO> buscarPorUsuario() {
        return solicitacaoRepository.buscarPorUsuario();
    }

    public List<SolicitacaoPorStatusDTO> buscarPorStatus() {
        return solicitacaoRepository.buscarPorStatus();
    }

// Método para gerar relatorio completo da lista
// ✅ Gerar lista de DTOs com filtros aplicados
@Transactional(readOnly = true)
public List<SolicitacaoRelatorioDTO> gerarRelatorio(String filtro) {
    return solicitacaoRepository.filtrarSemPaginacao(filtro).stream()
            .map(s -> new SolicitacaoRelatorioDTO(
                    s.getId(),
                    s.getDataSolicitacao(),
                    s.getStatus(),
                    s.getCarro().getPlaca(),
                    s.getMotorista().getNome(),
                    s.getUsuario().getNome(),
                    s.getSetor().getNome(),
                    s.getDestino().getNome(),
                    s.getKmInicial(),
                    s.getKmFinal(),
                    s.getHoraSaida(),
                    s.getHoraChegada()
            ))
            .toList();
}

    // ✅ Exportar Excel
    public ByteArrayInputStream exportarExcel(String filtro) throws IOException {
        List<SolicitacaoRelatorioDTO> solicitacoes = gerarRelatorio(filtro);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Solicitações");

            // Cabeçalho
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Data");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Carro");
            header.createCell(4).setCellValue("Motorista");
            header.createCell(5).setCellValue("Usuário");
            header.createCell(6).setCellValue("Setor");
            header.createCell(7).setCellValue("Destino");
            header.createCell(8).setCellValue("Km Inicial");
            header.createCell(9).setCellValue("Km Final");
            header.createCell(10).setCellValue("Hora Saída");
            header.createCell(11).setCellValue("Hora Chegada");

            // Dados
            int rowIdx = 1;
            for (SolicitacaoRelatorioDTO s : solicitacoes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.id());
                row.createCell(1).setCellValue(s.dataSolicitacao() != null ? s.dataSolicitacao().toString() : "");
                row.createCell(2).setCellValue(s.status());
                row.createCell(3).setCellValue(s.carro());
                row.createCell(4).setCellValue(s.motorista());
                row.createCell(5).setCellValue(s.usuario());
                row.createCell(6).setCellValue(s.setor());
                row.createCell(7).setCellValue(s.destino());
                row.createCell(8).setCellValue(s.kmInicial() != null ? s.kmInicial() : 0);
                row.createCell(9).setCellValue(s.kmFinal() != null ? s.kmFinal() : 0);
                row.createCell(10).setCellValue(s.horaSaida() != null ? s.horaSaida().toString() : "");
                row.createCell(11).setCellValue(s.horaChegada() != null ? s.horaChegada().toString() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ✅ Exportar CSV
    public ByteArrayInputStream exportarCsv(String filtro) {
        List<SolicitacaoRelatorioDTO> solicitacoes = gerarRelatorio(filtro);

        StringBuilder sb = new StringBuilder();
        sb.append("ID;Data;Status;Carro;Motorista;Usuário;Setor;Destino;Km Inicial;Km Final;Hora Saída;Hora Chegada\n");

        for (SolicitacaoRelatorioDTO s : solicitacoes) {
            sb.append(s.id()).append(";")
                    .append(s.dataSolicitacao() != null ? s.dataSolicitacao() : "").append(";")
                    .append(s.status()).append(";")
                    .append(s.carro()).append(";")
                    .append(s.motorista()).append(";")
                    .append(s.usuario()).append(";")
                    .append(s.setor()).append(";")
                    .append(s.destino()).append(";")
                    .append(s.kmInicial() != null ? s.kmInicial() : "").append(";")
                    .append(s.kmFinal() != null ? s.kmFinal() : "").append(";")
                    .append(s.horaSaida() != null ? s.horaSaida() : "").append(";")
                    .append(s.horaChegada() != null ? s.horaChegada() : "").append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    // ✅ Exportar PDF
    public ByteArrayInputStream exportarPdf(String filtro) {
        List<SolicitacaoRelatorioDTO> solicitacoes = gerarRelatorio(filtro);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // formato paisagem
            PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            float margin = 30;
            float yStart = pageSize.getUpperRightY() - margin;
            float tableWidth = pageSize.getWidth() - 2 * margin;
            float rowHeight = 18;

            // colunas (ajustei largura agora que temos mais espaço)
            String[] colunas = {"ID", "Data", "Status", "Carro", "Motorista", "Setor", "Destino", "Km Total"};
            float[] colWidths = {15, 60, 60, 50, 200, 180, 150,30};

            int rowIndex = 0;
            int pageNumber = 1;

            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = yStart;

            // 🔹 Carrega a imagem da logo
            InputStream logoStream = getClass().getResourceAsStream("/static/images/logo_hrg.png");
            if (logoStream != null) {
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo");

                float logoWidth = 83;   // largura desejada
                float logoHeight = 23;  // altura desejada
                float logoX = margin;   // canto esquerdo da página
                float logoY = yStart -10 ; // topo da página

                // desenha a logo
                contentStream.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
            } else {
                System.err.println("⚠️ Logo não encontrada em /images/logo_hrg.png");
            }

            // título
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Relatório de Solicitações");
            contentStream.endText();

            yPosition -= 30;

            // cabeçalho
            yPosition = desenharCabecalho(contentStream, margin, yPosition, tableWidth, rowHeight, colunas, colWidths);

            // linhas
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            for (SolicitacaoRelatorioDTO s : solicitacoes) {
                float nextX = margin;

                if (rowIndex % 2 == 0) {
                    contentStream.setNonStrokingColor(245, 245, 245);
                    contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0);
                }

                String[] valores = {
                        String.valueOf(s.id()),
                        s.dataSolicitacao() != null ? s.dataSolicitacao().toString() : "",
                        s.status(),
                        s.carro(),
                        s.motorista(),
                        s.setor(),
                        s.destino(),
                        //s.kmInicial() != null ? s.kmInicial().toString() : "-",
                        //s.kmFinal() != null ? s.kmFinal().toString() : "-",
                        s.getKmTotal() // ✅ campo calculado tratado
                };

                for (int i = 0; i < valores.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(nextX + 2, yPosition - 12);
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
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                }
            }

            desenharRodape(contentStream, pageSize, margin, pageNumber);
            contentStream.close();

            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF de Solicitações", e);
        }
    }

    // ✅ Cabeçalho
    private float desenharCabecalho(PDPageContentStream cs, float margin, float yPosition,
                                    float tableWidth, float rowHeight, String[] colunas, float[] colWidths) throws IOException {
        cs.setNonStrokingColor(200, 200, 200);
        cs.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0);

        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        float nextX = margin;
        for (int i = 0; i < colunas.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(nextX + 2, yPosition - 12);
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
        cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);

        String dataHora = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        cs.beginText();
        cs.newLineAtOffset(margin, margin - 15);
        cs.showText("Gerado em: " + dataHora);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(pageSize.getWidth() - margin - 50, margin - 15);
        cs.showText("Página " + pageNumber);
        cs.endText();
    }




}
