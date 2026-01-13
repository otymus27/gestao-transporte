package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Motorista.DTO.MotoristaRelatorioDTO;
import com.br.sistema.repositories.MotoristaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
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

    // ✅ PADRÃO SIMPLES: só carrega .jasper (não compila jrxml em runtime)
    private static final String CAMINHO_RELATORIO = "reports/motoristas/rel_motorista.jasper";

    private final MotoristaRepository motoristaRepository;

    // ==========================
    // RELATÓRIO SIMPLES (PDF/EXCEL)
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasSimples() {
        return gerarRelatorioMotoristasPdf(null);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasPdf(String filtroDescricao) {
        List<MotoristaRelatorioDTO> dados = motoristaRepository.listarParaRelatorio();
        String desc = (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados";
        return gerarPdf(dados, desc);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioMotoristasExcel(String filtroDescricao) {
        List<MotoristaRelatorioDTO> dados = motoristaRepository.listarParaRelatorio();
        String desc = (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados";
        return gerarExcel(dados, desc);
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

    // ==========================
    // CORE (CARREGA + FILL + EXPORT)
    // ==========================

    private JasperReport carregarRelatorio() {
        try {
            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            if (!resource.exists()) {
                // ✅ Erro claro (resolve 90% dos "não gera relatório")
                throw new RuntimeException("Relatório .jasper não encontrado no classpath: " + CAMINHO_RELATORIO);
            }

            try (InputStream is = resource.getInputStream()) {
                return (JasperReport) JRLoader.loadObject(is);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar relatório .jasper: " + CAMINHO_RELATORIO + " - " + e.getMessage(), e);
        }
    }

    private Map<String, Object> montarParams(String filtroDescricao) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("NOME_USUARIO", obterNomeUsuarioLogado());
            params.put("FILTRO_DESCRICAO", (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados");
            params.put("LOGO", new ClassPathResource("reports/images/logo_hrg.png").getInputStream());
            return params;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar logo do relatório: " + e.getMessage(), e);
        }
    }

    private byte[] gerarPdf(List<MotoristaRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de motoristas (PDF).");
            }

            JasperReport jasperReport = carregarRelatorio();
            Map<String, Object> params = montarParams(filtroDescricao);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

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

            JasperReport jasperReport = carregarRelatorio();
            Map<String, Object> params = montarParams(filtroDescricao);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
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

    // ==========================
    // AUXILIARES
    // ==========================

    private String obterNomeUsuarioLogado() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return "Usuário não autenticado";
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }

            String name = auth.getName();
            return name != null ? name : "Usuário não autenticado";

        } catch (Exception e) {
            log.warn("Não foi possível obter o usuário logado para o relatório", e);
            return "Usuário não identificado";
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
