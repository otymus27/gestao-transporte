package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Motorista.DTO.MotoristaRelatorioDTO;
import com.br.sistema.repositories.MotoristaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioMotoristaService {

    private static final String CAMINHO_RELATORIO = "reports/motoristas/rel_motorista.jrxml";

    private final MotoristaRepository motoristaRepository;

    /**
     * Mantido para compatibilidade.
     * Gera o PDF usando usuário logado e sem filtro específico.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasSimples() {
        return gerarRelatorioMotoristasPdf(null);
    }

    /**
     * Gera PDF de motoristas usando:
     * - usuário logado (buscado do SecurityContext)
     * - descrição opcional de filtro para exibir no cabeçalho
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasPdf(String filtroDescricao) {
        try {
            // 1. Busca dados
            List<MotoristaRelatorioDTO> dados = motoristaRepository.listarParaRelatorio();

            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de motoristas.");
            }

            // 2. Carrega o JRXML do classpath
            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            try (InputStream jrxmlStream = resource.getInputStream()) {

                // 3. Compila relatório
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                // 4. Monta parâmetros
                Map<String, Object> params = new HashMap<>();
                params.put("NOME_USUARIO", obterNomeUsuarioLogado());
                params.put("FILTRO_DESCRICAO",
                        filtroDescricao != null && !filtroDescricao.isBlank()
                                ? filtroDescricao
                                : "Sem filtros aplicados");

                // 5. DataSource com a lista de DTOs
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);

                // 6. Preenche o relatório
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

                // 7. Exporta para PDF
                return JasperExportManager.exportReportToPdf(jasperPrint);
            }

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de motoristas em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de motoristas em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Gera o relatório de motoristas em Excel (XLSX),
     * usando usuário logado e descrição opcional de filtros.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasExcel(String filtroDescricao) {
        try {
            // 1. Busca dados
            List<MotoristaRelatorioDTO> dados = motoristaRepository.listarParaRelatorio();

            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de motoristas (Excel).");
            }

            // 2. Carrega o JRXML
            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            try (InputStream jrxmlStream = resource.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                // 3. Compila
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                // 4. Parâmetros
                Map<String, Object> params = new HashMap<>();
                params.put("NOME_USUARIO", obterNomeUsuarioLogado());
                params.put("FILTRO_DESCRICAO",
                        filtroDescricao != null && !filtroDescricao.isBlank()
                                ? filtroDescricao
                                : "Sem filtros aplicados");

                // 5. DataSource
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);

                // 6. Preenche
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

                // 7. Exporta para Excel (XLSX)
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setDetectCellType(true);
                configuration.setCollapseRowSpan(false);
                configuration.setWhitePageBackground(false);
                configuration.setRemoveEmptySpaceBetweenRows(true);
                exporter.setConfiguration(configuration);

                exporter.exportReport();

                return out.toByteArray();
            }

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de motoristas em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de motoristas em Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém o nome do usuário logado a partir do SecurityContext.
     * Adapte esse método se você tiver uma entidade Usuario customizada.
     */
    private String obterNomeUsuarioLogado() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return "Usuário não autenticado";
            }

            Object principal = auth.getPrincipal();

            // Caso clássico: UserDetails
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }

            // Fallback: usa o próprio name do Authentication
            String name = auth.getName();
            return name != null ? name : "Usuário não autenticado";

        } catch (Exception e) {
            log.warn("Não foi possível obter o usuário logado para o relatório", e);
            return "Usuário não identificado";
        }
    }

    // ==========================
    // RELATÓRIO FILTRADO (PDF/EXCEL)
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasPdfFiltrado(String matricula, String nome, String telefone, Boolean ativo) {
        List<MotoristaRelatorioDTO> dados =
                motoristaRepository.listarParaRelatorioFiltrado(matricula, nome, telefone, ativo);

        String filtroDescricao = montarDescricaoFiltro(matricula, nome, telefone, ativo);
        return gerarPdf(dados, filtroDescricao);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasExcelFiltrado(String matricula, String nome, String telefone, Boolean ativo) {
        List<MotoristaRelatorioDTO> dados =
                motoristaRepository.listarParaRelatorioFiltrado(matricula, nome, telefone, ativo);

        String filtroDescricao = montarDescricaoFiltro(matricula, nome, telefone, ativo);
        return gerarExcel(dados, filtroDescricao);
    }

    private byte[] gerarPdf(List<MotoristaRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de motoristas (PDF).");
            }

            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            try (InputStream jrxmlStream = resource.getInputStream()) {
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                Map<String, Object> params = new HashMap<>();
                params.put("NOME_USUARIO", obterNomeUsuarioLogado());
                params.put("FILTRO_DESCRICAO", filtroDescricao);

                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

                return JasperExportManager.exportReportToPdf(jasperPrint);
            }
        } catch (Exception e) {
            log.error("Erro ao gerar relatório de motoristas em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de motoristas em PDF: " + e.getMessage(), e);
        }
    }

    private byte[] gerarExcel(List<MotoristaRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de motoristas (Excel).");
            }

            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            try (InputStream jrxmlStream = resource.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                Map<String, Object> params = new HashMap<>();
                params.put("NOME_USUARIO", obterNomeUsuarioLogado());
                params.put("FILTRO_DESCRICAO", filtroDescricao);

                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setDetectCellType(true);
                configuration.setCollapseRowSpan(false);
                configuration.setWhitePageBackground(false);
                configuration.setRemoveEmptySpaceBetweenRows(true);
                exporter.setConfiguration(configuration);

                exporter.exportReport();

                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("Erro ao gerar relatório de motoristas em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de motoristas em Excel: " + e.getMessage(), e);
        }
    }

    private String montarDescricaoFiltro(String matricula, String nome, String telefone, Boolean ativo) {
        StringBuilder sb = new StringBuilder("Filtros: ");
        boolean algum = false;

        if (matricula != null && !matricula.isBlank()) { sb.append("Matrícula=").append(matricula).append(" | "); algum = true; }
        if (nome != null && !nome.isBlank()) { sb.append("Nome=").append(nome).append(" | "); algum = true; }
        if (telefone != null && !telefone.isBlank()) { sb.append("Telefone=").append(telefone).append(" | "); algum = true; }
        if (ativo != null) { sb.append("Ativo=").append(ativo ? "Sim" : "Não").append(" | "); algum = true; }

        if (!algum) return "Sem filtros aplicados";
        return sb.substring(0, sb.length() - 3);
    }

    // ==========================
    // CONSULTA (LISTA / PÁGINA) PARA TELA DE RELATÓRIO
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public List<MotoristaRelatorioDTO> consultarMotoristasParaRelatorio(
            String matricula, String nome, String telefone, Boolean ativo
    ) {
        return motoristaRepository.listarParaRelatorioFiltrado(matricula, nome, telefone, ativo);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Page<MotoristaRelatorioDTO> consultarMotoristasParaRelatorioPaginado(
            String matricula, String nome, String telefone, Boolean ativo,
            int page, int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return motoristaRepository.listarParaConsultaRelatorioPaginado(matricula, nome, telefone, ativo, pageable);
    }
}
