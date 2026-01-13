package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRelatorioDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.repositories.SolicitacaoRepository;
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
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioSolicitacaoService {

    // ✅ PADRÃO SIMPLES: só carrega .jasper (não compila jrxml em runtime)
    private static final String CAMINHO_RELATORIO = "reports/solicitacoes/rel_motorista.jasper";

    private final SolicitacaoRepository solicitacaoRepository;

    // ==========================
    // RELATÓRIO SIMPLES (PDF)
    // ==========================

    /**
     * Mantido para compatibilidade.
     * Gera o PDF usando usuário logado e sem filtro específico.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesSimples() {
        return gerarRelatorioSolicitacoesPdf(null);
    }

    /**
     * Gera PDF de solicitações sem filtro (todas).
     * OBS: se a base for grande, prefira os métodos filtrados.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesPdf(String filtroDescricao) {
        try {
            // 1) Busca dados (tudo)
            List<Solicitacao> solicitacoes = solicitacaoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<SolicitacaoRelatorioDTO> dados = mapearParaDTO(solicitacoes);

            if (dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de solicitações (PDF).");
            }

            // 2) Carrega JRXML
            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            try (InputStream jrxmlStream = resource.getInputStream()) {
                // 3) Compila
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                // 4) Params
                Map<String, Object> params = new HashMap<>();
                params.put("NOME_USUARIO", obterNomeUsuarioLogado());
                params.put("FILTRO_DESCRICAO",
                        filtroDescricao != null && !filtroDescricao.isBlank()
                                ? filtroDescricao
                                : "Sem filtros aplicados");

                // 5) DataSource
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);

                // 6) Preenche
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

                // 7) Exporta PDF
                return JasperExportManager.exportReportToPdf(jasperPrint);
            }

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de solicitações em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de solicitações em PDF: " + e.getMessage(), e);
        }
    }

    // ==========================
    // RELATÓRIO SIMPLES (EXCEL)
    // ==========================

    /**
     * Gera o relatório de solicitações em Excel (XLSX).
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesExcel(String filtroDescricao) {
        try {
            // 1) Busca dados
            List<Solicitacao> solicitacoes = solicitacaoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<SolicitacaoRelatorioDTO> dados = mapearParaDTO(solicitacoes);

            if (dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de solicitações (Excel).");
            }

            // 2) Carrega JRXML
            ClassPathResource resource = new ClassPathResource(CAMINHO_RELATORIO);

            try (InputStream jrxmlStream = resource.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                // 3) Compila
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                // 4) Params
                Map<String, Object> params = new HashMap<>();
                params.put("NOME_USUARIO", obterNomeUsuarioLogado());
                params.put("FILTRO_DESCRICAO",
                        filtroDescricao != null && !filtroDescricao.isBlank()
                                ? filtroDescricao
                                : "Sem filtros aplicados");

                // 5) DataSource
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados);

                // 6) Preenche
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

                // 7) Exporta XLSX
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
            log.error("Erro ao gerar relatório de solicitações em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de solicitações em Excel: " + e.getMessage(), e);
        }
    }

    // ==========================
    // RELATÓRIO FILTRADO (PDF/EXCEL)
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesPdfFiltrado(String filtro, LocalDate dataInicio, LocalDate dataFim) {
        List<SolicitacaoRelatorioDTO> dados = consultarSolicitacoesParaRelatorio(filtro, dataInicio, dataFim);
        String filtroDescricao = montarDescricaoFiltro(filtro, dataInicio, dataFim);
        return gerarPdf(dados, filtroDescricao);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesExcelFiltrado(String filtro, LocalDate dataInicio, LocalDate dataFim) {
        List<SolicitacaoRelatorioDTO> dados = consultarSolicitacoesParaRelatorio(filtro, dataInicio, dataFim);
        String filtroDescricao = montarDescricaoFiltro(filtro, dataInicio, dataFim);
        return gerarExcel(dados, filtroDescricao);
    }

    private byte[] gerarPdf(List<SolicitacaoRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de solicitações (PDF).");
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
            log.error("Erro ao gerar relatório de solicitações em PDF", e);
            throw new RuntimeException("Erro ao gerar relatório de solicitações em PDF: " + e.getMessage(), e);
        }
    }

    private byte[] gerarExcel(List<SolicitacaoRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório de solicitações (Excel).");
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
            log.error("Erro ao gerar relatório de solicitações em Excel", e);
            throw new RuntimeException("Erro ao gerar relatório de solicitações em Excel: " + e.getMessage(), e);
        }
    }

    // ==========================
    // CONSULTA (LISTA / PÁGINA) PARA TELA DE RELATÓRIO
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public List<SolicitacaoRelatorioDTO> consultarSolicitacoesParaRelatorio(
            String filtro,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        List<Solicitacao> base = solicitacaoRepository.filtrarSemPaginacao(filtro);

        // filtro por datas (LocalDate)
        List<Solicitacao> filtradas = aplicarFiltroDatas(base, dataInicio, dataFim);

        // map DTO
        return mapearParaDTO(filtradas);
    }

    /**
     * Paginação em memória (no mesmo padrão do template).
     * Se a base crescer muito, a gente cria uma query paginada projetando direto em DTO.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public Page<SolicitacaoRelatorioDTO> consultarSolicitacoesParaRelatorioPaginado(
            String filtro,
            LocalDate dataInicio,
            LocalDate dataFim,
            int page,
            int size
    ) {
        List<SolicitacaoRelatorioDTO> dados = consultarSolicitacoesParaRelatorio(filtro, dataInicio, dataFim);

        int start = Math.min(page * size, dados.size());
        int end = Math.min(start + size, dados.size());

        List<SolicitacaoRelatorioDTO> content = dados.subList(start, end);

        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return new PageImpl<>(content, pageable, dados.size());
    }

    // ==========================
    // HELPERS
    // ==========================

    private List<Solicitacao> aplicarFiltroDatas(List<Solicitacao> base, LocalDate inicio, LocalDate fim) {
        if (base == null || base.isEmpty()) return Collections.emptyList();

        if (inicio != null && fim != null) {
            return base.stream()
                    .filter(s -> s.getDataSolicitacao() != null
                            && !s.getDataSolicitacao().isBefore(inicio)
                            && !s.getDataSolicitacao().isAfter(fim))
                    .collect(Collectors.toList());
        }

        if (inicio != null) {
            return base.stream()
                    .filter(s -> s.getDataSolicitacao() != null
                            && !s.getDataSolicitacao().isBefore(inicio))
                    .collect(Collectors.toList());
        }

        if (fim != null) {
            return base.stream()
                    .filter(s -> s.getDataSolicitacao() != null
                            && !s.getDataSolicitacao().isAfter(fim))
                    .collect(Collectors.toList());
        }

        return base;
    }

    private List<SolicitacaoRelatorioDTO> mapearParaDTO(List<Solicitacao> solicitacoes) {
        if (solicitacoes == null || solicitacoes.isEmpty()) return Collections.emptyList();

        return solicitacoes.stream().map(s -> {
            SolicitacaoRelatorioDTO dto = new SolicitacaoRelatorioDTO();

            dto.setId(s.getId());
            dto.setDataSolicitacao(s.getDataSolicitacao());
            dto.setStatus(s.getStatus());

            // --- Relacionamentos (texto pronto pro relatório)
            dto.setCarro(montarDescricaoCarro(s));
            dto.setMotorista(s.getMotorista() != null ? s.getMotorista().getNome() : "-");
            dto.setUsuario(s.getUsuario() != null ? s.getUsuario().getNome() : "-");
            dto.setSetor(s.getSetor() != null ? s.getSetor().getNome() : "-");
            dto.setDestino(s.getDestino() != null ? s.getDestino().getNome() : "-");

            // --- Campos de quilometragem e horários (ajuste os getters conforme sua entidade)
            dto.setKmInicial(s.getKmInicial());
            dto.setKmFinal(s.getKmFinal());
            dto.setHoraSaida(s.getHoraSaida());
            dto.setHoraChegada(s.getHoraChegada());

            return dto;
        }).collect(Collectors.toList());
    }

    private String montarDescricaoCarro(Solicitacao s) {
        if (s.getCarro() == null) return "-";

        String placa = safe(s.getCarro().getPlaca());
        String marca = safe(s.getCarro().getMarca());
        String modelo = safe(s.getCarro().getModelo());

        // Ex.: "ABC-1234 - FIAT - UNO"
        String base = String.join(" - ", placa, marca, modelo).replaceAll("(\\s*-\\s*)+$", "");
        return base.isBlank() ? "-" : base;
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String montarDescricaoFiltro(String filtro, LocalDate inicio, LocalDate fim) {
        List<String> partes = new ArrayList<>();

        if (filtro != null && !filtro.isBlank()) {
            partes.add("Filtro=" + filtro);
        }
        if (inicio != null) {
            partes.add("Data Início=" + inicio);
        }
        if (fim != null) {
            partes.add("Data Fim=" + fim);
        }

        return partes.isEmpty() ? "Sem filtros aplicados" : "Filtros: " + String.join(" | ", partes);
    }

    /**
     * Obtém o nome do usuário logado a partir do SecurityContext.
     */
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
}
