package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Usuario.DTO.ProducaoUsuarioDTO;
import com.br.sistema.repositories.FichaSolicitacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Relatório de Produção por Usuário: quantidade de fichas e de solicitações
 * criadas por cada usuário em um período (comprovação de produção de quem digitou).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioProducaoService {

    private static final String CAMINHO_RELATORIO = "reports/producao/rel_producao_usuario.jrxml";
    private static final String LOGO_PATH = "reports/images/logo_hrg.png";
    private static final String JASPER_JDT_COMPILER = "net.sf.jasperreports.jdt.JRJdtCompiler";

    private final FichaSolicitacaoRepository fichaSolicitacaoRepository;

    // ==========================
    // RELATÓRIO FILTRADO (PDF/EXCEL)
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioProducaoPdfFiltrado(String nomeUsuario, LocalDate dataInicio, LocalDate dataFim) {
        List<ProducaoUsuarioDTO> dados = consultarProducaoParaRelatorio(nomeUsuario, dataInicio, dataFim);
        return gerarPdf(dados, montarDescricaoFiltro(nomeUsuario, dataInicio, dataFim));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioProducaoExcelFiltrado(String nomeUsuario, LocalDate dataInicio, LocalDate dataFim) {
        List<ProducaoUsuarioDTO> dados = consultarProducaoParaRelatorio(nomeUsuario, dataInicio, dataFim);
        return gerarExcel(dados, montarDescricaoFiltro(nomeUsuario, dataInicio, dataFim));
    }

    // ==========================
    // CONSULTA
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProducaoUsuarioDTO> consultarProducaoParaRelatorio(
            String nomeUsuario, LocalDate dataInicio, LocalDate dataFim) {
        return fichaSolicitacaoRepository.buscarProducaoPorUsuario(nomeUsuario, dataInicio, dataFim);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Page<ProducaoUsuarioDTO> consultarProducaoParaRelatorioPaginado(
            String nomeUsuario, LocalDate dataInicio, LocalDate dataFim, int page, int size) {
        List<ProducaoUsuarioDTO> dados = consultarProducaoParaRelatorio(nomeUsuario, dataInicio, dataFim);

        int start = Math.min(page * size, dados.size());
        int end = Math.min(start + size, dados.size());

        return new PageImpl<>(
                dados.subList(start, end),
                PageRequest.of(page, size, Sort.by("usuarioNome")),
                dados.size()
        );
    }

    // ==========================
    // CORE (CARREGA + FILL + EXPORT)
    // ==========================

    private byte[] gerarPdf(List<ProducaoUsuarioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de produção por usuário (PDF).");
            }

            JasperReport jasperReport = carregarRelatorio();
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    montarParams(filtroDescricao),
                    new JRBeanCollectionDataSource(dados == null ? new ArrayList<>() : dados)
            );
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de produção por usuário em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de produção por usuário em PDF: " + e.getMessage(), e);
        }
    }

    private byte[] gerarExcel(List<ProducaoUsuarioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de produção por usuário (Excel).");
            }

            JasperReport jasperReport = carregarRelatorio();
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    montarParams(filtroDescricao),
                    new JRBeanCollectionDataSource(dados == null ? new ArrayList<>() : dados)
            );

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

                SimpleXlsxReportConfiguration conf = new SimpleXlsxReportConfiguration();
                conf.setDetectCellType(true);
                conf.setCollapseRowSpan(false);
                conf.setWhitePageBackground(false);
                conf.setRemoveEmptySpaceBetweenRows(true);
                exporter.setConfiguration(conf);
                exporter.exportReport();

                return out.toByteArray();
            }

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de produção por usuário em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de produção por usuário em Excel: " + e.getMessage(), e);
        }
    }

    private JasperReport carregarRelatorio() {
        try {
            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);
            if (!resource.exists()) throw new RuntimeException("Relatório não encontrado: " + CAMINHO_RELATORIO);
            try (InputStream is = resource.getInputStream()) {
                DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
                context.setProperty(JRCompiler.COMPILER_CLASS, JASPER_JDT_COMPILER);
                context.setProperty(JRCompiler.COMPILER_PREFIX + "java", JASPER_JDT_COMPILER);
                return JasperCompileManager.getInstance(context).compile(is);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar relatório: " + CAMINHO_RELATORIO + " - " + e.getMessage(), e);
        }
    }

    private Map<String, Object> montarParams(String filtroDescricao) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("NOME_USUARIO", obterNomeUsuarioLogado());
            params.put("FILTRO_DESCRICAO",
                    (filtroDescricao != null && !filtroDescricao.isBlank())
                            ? filtroDescricao : "Sem filtros aplicados");
            params.put("LOGO", new ClassPathResource(LOGO_PATH).getInputStream());
            return params;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao montar parâmetros do relatório: " + e.getMessage(), e);
        }
    }

    private String montarDescricaoFiltro(String nomeUsuario, LocalDate inicio, LocalDate fim) {
        List<String> partes = new ArrayList<>();
        if (nomeUsuario != null && !nomeUsuario.isBlank()) partes.add("Usuário=" + nomeUsuario);
        if (inicio != null) partes.add("Data Início=" + inicio);
        if (fim != null) partes.add("Data Fim=" + fim);
        return partes.isEmpty() ? "Sem filtros aplicados" : "Filtros: " + String.join(" | ", partes);
    }

    private String obterNomeUsuarioLogado() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return "Usuário não autenticado";
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails ud) return ud.getUsername();
            String name = auth.getName();
            return name != null ? name : "Usuário não autenticado";
        } catch (Exception e) {
            log.warn("Não foi possível obter o usuário logado para o relatório", e);
            return "Usuário não identificado";
        }
    }
}
