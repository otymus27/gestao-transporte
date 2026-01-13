package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Setor.DTO.SetorRelatorioDTO;
import com.br.sistema.repositories.SetorRepository;
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
public class RelatorioSetorService {

    private static final String CAMINHO_RELATORIO = "reports/setor/rel_setor.jasper";

    private final SetorRepository setorRepository;

    /**
     * Mantido para compatibilidade.
     * Gera o PDF usando usuário logado e sem filtro específico.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSetoresSimples() {
        return gerarRelatorioSetoresPdf(null);
    }

    /**
     * Gera PDF de setores usando:
     * - usuário logado (buscado do SecurityContext)
     * - descrição opcional de filtro para exibir no cabeçalho
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSetoresPdf(String filtroDescricao) {

        try {
            List<SetorRelatorioDTO> dados = setorRepository.listarParaRelatorio();

            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de setores.");
            }

            String desc = (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados";
            return gerarPdf(dados, desc);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de setores em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de setores em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Gera o relatório de setores em Excel (XLSX),
     * usando usuário logado e descrição opcional de filtros.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSetoresExcel(String filtroDescricao) {
        try {
            // 1. Busca dados
            List<SetorRelatorioDTO> dados = setorRepository.listarParaRelatorio();

            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de setores (Excel).");
            }

            String desc = (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados";
            return gerarExcel(dados, desc);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de setores em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de setores em Excel: " + e.getMessage(), e);
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
    public byte[] gerarRelatorioSetoresPdfFiltrado(String nome) {
        List<SetorRelatorioDTO> dados = setorRepository.listarParaRelatorioFiltrado(nome);
        String filtroDescricao = montarDescricaoFiltro(nome);
        return gerarPdf(dados, filtroDescricao);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSetoresExcelFiltrado(String nome) {
        List<SetorRelatorioDTO> dados = setorRepository.listarParaRelatorioFiltrado(nome);
        String filtroDescricao = montarDescricaoFiltro(nome);
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

    private byte[] gerarPdf(List<SetorRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de setores (PDF).");
            }

            JasperReport jasperReport = carregarRelatorio();
            Map<String, Object> params = montarParams(filtroDescricao);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de setores em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de setores em PDF: " + e.getMessage(), e);
        }
    }

    private byte[] gerarExcel(List<SetorRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de setores (Excel).");
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
            log.error("Erro ao gerar relatório de setores em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de setores em Excel: " + e.getMessage(), e);
        }
    }

    private String montarDescricaoFiltro(String nome) {
        if (nome != null && !nome.isBlank()) {
            return "Filtros: Nome=" + nome;
        }
        return "Sem filtros aplicados";
    }

    // ==========================
    // CONSULTA (LISTA / PÁGINA) PARA TELA DE RELATÓRIO
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public List<SetorRelatorioDTO> consultarSetoresParaRelatorio(String nome) {
        return setorRepository.listarParaRelatorioFiltrado(nome);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Page<SetorRelatorioDTO> consultarSetoresParaRelatorioPaginado(
            String nome,
            int page,
            int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return setorRepository.listarParaConsultaRelatorioPaginado(nome, pageable);
    }
}
