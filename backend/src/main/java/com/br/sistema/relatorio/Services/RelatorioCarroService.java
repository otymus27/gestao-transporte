package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Carro.DTO.CarroRelatorioDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaRelatorioDTO;
import com.br.sistema.repositories.CarroRepository;
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
public class RelatorioCarroService {

    // ✅ PADRÃO SIMPLES: só carrega .jasper (não compila jrxml em runtime)
    private static final String CAMINHO_RELATORIO = "reports/carros/rel_carros.jasper";

    private final CarroRepository carroRepository;

    /**
     * Mantido para compatibilidade.
     * Gera o PDF usando usuário logado e sem filtro específico.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioCarrosSimples() {
        return gerarRelatorioCarrosPdf(null);
    }

    /**
     * Gera PDF de carros usando:
     * - usuário logado (buscado do SecurityContext)
     * - descrição opcional de filtro para exibir no cabeçalho
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioCarrosPdf(String filtroDescricao) {
        List<CarroRelatorioDTO> dados = carroRepository.listarParaRelatorio();
        String desc = (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados";
        return gerarPdf(dados, desc);
    }

    /**
     * Gera o relatório de carros em Excel (XLSX),
     * usando usuário logado e descrição opcional de filtros.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioCarrosExcel(String filtroDescricao) {

        List<CarroRelatorioDTO> dados = carroRepository.listarParaRelatorio();
        String desc = (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados";
        return gerarExcel(dados, desc);

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
                return userDetails.getUsername(); // ou outro dado
            }

            // Fallback: usa o próprio name do Authentication
            String name = auth.getName();
            return name != null ? name : "Usuário não autenticado";

        } catch (Exception e) {
            log.warn("Não foi possível obter o usuário logado para o relatório", e);
            return "Usuário não identificado";
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioCarrosPdfFiltrado(String placa, String marca, String modelo, String tipo) {
        List<CarroRelatorioDTO> dados = carroRepository.listarParaRelatorioFiltrado(placa, marca, modelo, tipo);
        String filtroDescricao = montarDescricaoFiltro(placa, marca, modelo, tipo);
        return gerarPdf(dados, filtroDescricao);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioCarrosExcelFiltrado(String placa, String marca, String modelo, String tipo) {
        List<CarroRelatorioDTO> dados = carroRepository.listarParaRelatorioFiltrado(placa, marca, modelo, tipo);
        String filtroDescricao = montarDescricaoFiltro(placa, marca, modelo, tipo);
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

    private byte[] gerarPdf(List<CarroRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de carros (PDF).");
            }

            JasperReport jasperReport = carregarRelatorio();
            Map<String, Object> params = montarParams(filtroDescricao);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("Erro ao gerar relatório de carros em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de carros em PDF: " + e.getMessage(), e);
        }
    }

    private byte[] gerarExcel(List<CarroRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de carros (Excel).");
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
            log.error("Erro ao gerar relatório de carros em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de carros em Excel: " + e.getMessage(), e);
        }
    }

    private String montarDescricaoFiltro(String placa, String marca, String modelo, String tipo) {
        StringBuilder sb = new StringBuilder("Filtros: ");
        boolean algum = false;

        if (placa != null && !placa.isBlank()) { sb.append("Placa=").append(placa).append(" | "); algum = true; }
        if (marca != null && !marca.isBlank()) { sb.append("Marca=").append(marca).append(" | "); algum = true; }
        if (modelo != null && !modelo.isBlank()) { sb.append("Modelo=").append(modelo).append(" | "); algum = true; }
        if (tipo != null && !tipo.isBlank()) { sb.append("Tipo=").append(tipo).append(" | "); algum = true; }

        if (!algum) return "Sem filtros aplicados";
        return sb.substring(0, sb.length() - 3);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<CarroRelatorioDTO> consultarCarrosParaRelatorio(String placa, String marca, String modelo, String tipo) {
        return carroRepository.listarParaRelatorioFiltrado(placa, marca, modelo, tipo);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Page<CarroRelatorioDTO> consultarCarrosParaRelatorioPaginado(
            String placa, String marca, String modelo, String tipo,
            int page, int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return carroRepository.listarParaConsultaRelatorioPaginado(placa, marca, modelo, tipo, pageable);
    }


}