package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRelatorioDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.relatorio.enums.TipoRelatorioSolicitacao;
import com.br.sistema.repositories.SolicitacaoRepository;
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

    // ✅ Template padrão (mantém compatibilidade com endpoints antigos)
    private static final TipoRelatorioSolicitacao TIPO_PADRAO = TipoRelatorioSolicitacao.SIMPLES;

    // ✅ Mapa dos relatórios compilados (.jasper)
    // ⚠️ AJUSTE os nomes exatamente como estão no seu resources
    private static final Map<TipoRelatorioSolicitacao, String> RELATORIOS = Map.of(
            TipoRelatorioSolicitacao.SIMPLES, "reports/solicitacoes/rel_solicitacao.jasper",
            TipoRelatorioSolicitacao.POR_SETOR,     "reports/solicitacoes/rel_solicitacao_agrupado_por_setor.jasper",
            TipoRelatorioSolicitacao.POR_MOTORISTA, "reports/solicitacoes/rel_solicitacao_agrupado_por_motorista.jasper",
            TipoRelatorioSolicitacao.POR_CARRO,     "reports/solicitacoes/rel_solicitacao_agrupado_por_carro.jasper",
            TipoRelatorioSolicitacao.POR_DESTINO,   "reports/solicitacoes/rel_solicitacao_agrupado_por_destino.jasper"
    );

    private static final String LOGO_PATH = "reports/images/logo_hrg.png";

    private final SolicitacaoRepository solicitacaoRepository;

    // ==========================
    // COMPATIBILIDADE (ANTIGOS)
    // ==========================

    /**
     * Mantido: gera PDF "simples" (tipo padrão) sem filtros.
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesSimples() {
        return gerarRelatorioSolicitacoesPdfFiltrado(TIPO_PADRAO, null, null, null);
    }

    /**
     * Mantido: gera PDF filtrado (tipo padrão).
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesPdfFiltrado(String filtro, LocalDate dataInicio, LocalDate dataFim) {
        return gerarRelatorioSolicitacoesPdfFiltrado(TIPO_PADRAO, filtro, dataInicio, dataFim);
    }

    /**
     * Mantido: gera Excel filtrado (tipo padrão).
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesExcelFiltrado(String filtro, LocalDate dataInicio, LocalDate dataFim) {
        return gerarRelatorioSolicitacoesExcelFiltrado(TIPO_PADRAO, filtro, dataInicio, dataFim);
    }

    // ==========================
    // NOVOS (COM TIPO)
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesPdfFiltrado(
            TipoRelatorioSolicitacao tipo,
            String filtro,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        List<SolicitacaoRelatorioDTO> dados = consultarSolicitacoesParaRelatorio(filtro, dataInicio, dataFim);
        String filtroDescricao = montarDescricaoFiltro(filtro, dataInicio, dataFim);
        return gerarPdf(tipo, dados, filtroDescricao);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public byte[] gerarRelatorioSolicitacoesExcelFiltrado(
            TipoRelatorioSolicitacao tipo,
            String filtro,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        List<SolicitacaoRelatorioDTO> dados = consultarSolicitacoesParaRelatorio(filtro, dataInicio, dataFim);
        String filtroDescricao = montarDescricaoFiltro(filtro, dataInicio, dataFim);
        return gerarExcel(tipo, dados, filtroDescricao);
    }

    // ==========================
    // CORE (FILL + EXPORT)
    // ==========================

    private byte[] gerarPdf(TipoRelatorioSolicitacao tipo, List<SolicitacaoRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório (PDF). tipo={}", tipo);
            }

            // ✅ importante para o Jasper agrupar corretamente
            ordenarParaAgrupamento(dados, tipo);

            JasperReport jasperReport = carregarRelatorio(tipo);
            Map<String, Object> params = montarParams(filtroDescricao);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    params,
                    new JRBeanCollectionDataSource(dados)
            );

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório PDF tipo={}", tipo, e);
            throw new RuntimeException("Erro ao gerar relatório PDF (" + tipo + "): " + e.getMessage(), e);
        }
    }

    private byte[] gerarExcel(TipoRelatorioSolicitacao tipo, List<SolicitacaoRelatorioDTO> dados, String filtroDescricao) {
        try {
            if (dados == null || dados.isEmpty()) {
                log.warn("Nenhum dado encontrado para o relatório (Excel). tipo={}", tipo);
            }

            // ✅ importante para o Jasper agrupar corretamente
            ordenarParaAgrupamento(dados, tipo);

            JasperReport jasperReport = carregarRelatorio(tipo);
            Map<String, Object> params = montarParams(filtroDescricao);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    params,
                    new JRBeanCollectionDataSource(dados)
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
            log.error("Erro ao gerar relatório Excel tipo={}", tipo, e);
            throw new RuntimeException("Erro ao gerar relatório Excel (" + tipo + "): " + e.getMessage(), e);
        }
    }

    private JasperReport carregarRelatorio(TipoRelatorioSolicitacao tipo) {
        String caminho = RELATORIOS.get(tipo);
        if (caminho == null) {
            throw new RuntimeException("Tipo de relatório não mapeado: " + tipo);
        }

        try {
            ClassPathResource resource = new ClassPathResource(caminho);
            if (!resource.exists()) {
                throw new RuntimeException("Relatório .jasper não encontrado no classpath: " + caminho);
            }

            try (InputStream is = resource.getInputStream()) {
                return (JasperReport) JRLoader.loadObject(is);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar relatório .jasper: " + caminho + " - " + e.getMessage(), e);
        }
    }

    private Map<String, Object> montarParams(String filtroDescricao) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("NOME_USUARIO", obterNomeUsuarioLogado());
            params.put("FILTRO_DESCRICAO",
                    (filtroDescricao != null && !filtroDescricao.isBlank()) ? filtroDescricao : "Sem filtros aplicados");

            // ✅ Logo opcional (se falhar, lança erro claro)
            params.put("LOGO", new ClassPathResource(LOGO_PATH).getInputStream());
            return params;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao montar parâmetros do relatório: " + e.getMessage(), e);
        }
    }

    // ==========================
    // CONSULTA (LISTA / PÁGINA)
    // ==========================

    @Transactional(Transactional.TxType.REQUIRED)
    public List<SolicitacaoRelatorioDTO> consultarSolicitacoesParaRelatorio(
            String filtro,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        List<Solicitacao> base = solicitacaoRepository.filtrarSemPaginacao(filtro);
        List<Solicitacao> filtradas = aplicarFiltroDatas(base, dataInicio, dataFim);
        return mapearParaDTO(filtradas);
    }

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
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return new PageImpl<>(content, pageable, dados.size());
    }

    // ==========================
    // ORDENAÇÃO (GROUP DO JASPER)
    // ==========================

    private void ordenarParaAgrupamento(List<SolicitacaoRelatorioDTO> dados, TipoRelatorioSolicitacao tipo) {
        if (dados == null || dados.isEmpty()) return;

        // ✅ SEM AGRUPAMENTO → ordenação simples
        if (tipo == TipoRelatorioSolicitacao.SIMPLES) {
            dados.sort(
                    Comparator.comparing(SolicitacaoRelatorioDTO::getDataSolicitacao, Comparator.nullsLast(LocalDate::compareTo))
                            .thenComparing(SolicitacaoRelatorioDTO::getId, Comparator.nullsLast(Long::compareTo))
            );
            return;
        }

        // 🔽 AGRUPADOS
        Comparator<SolicitacaoRelatorioDTO> porGrupo = switch (tipo) {
            case POR_SETOR -> Comparator.comparing(SolicitacaoRelatorioDTO::getSetor, Comparator.nullsLast(String::compareToIgnoreCase));
            case POR_MOTORISTA -> Comparator.comparing(SolicitacaoRelatorioDTO::getMotorista, Comparator.nullsLast(String::compareToIgnoreCase));
            case POR_CARRO -> Comparator.comparing(SolicitacaoRelatorioDTO::getCarro, Comparator.nullsLast(String::compareToIgnoreCase));
            case POR_DESTINO -> Comparator.comparing(SolicitacaoRelatorioDTO::getDestino, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(SolicitacaoRelatorioDTO::getId);
        };

        Comparator<SolicitacaoRelatorioDTO> base = Comparator
                .comparing(SolicitacaoRelatorioDTO::getDataSolicitacao, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(SolicitacaoRelatorioDTO::getId, Comparator.nullsLast(Long::compareTo));

        dados.sort(porGrupo.thenComparing(base));
    }

    // ==========================
    // AUXILIARES (FILTRO / DTO)
    // ==========================

    private List<Solicitacao> aplicarFiltroDatas(List<Solicitacao> base, LocalDate inicio, LocalDate fim) {
        if (base == null || base.isEmpty()) return Collections.emptyList();

        return base.stream()
                .filter(s -> {
                    LocalDate d = s.getDataSolicitacao();
                    if (d == null) return false;
                    if (inicio != null && d.isBefore(inicio)) return false;
                    if (fim != null && d.isAfter(fim)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<SolicitacaoRelatorioDTO> mapearParaDTO(List<Solicitacao> solicitacoes) {
        if (solicitacoes == null || solicitacoes.isEmpty()) return Collections.emptyList();

        return solicitacoes.stream().map(s -> {
            SolicitacaoRelatorioDTO dto = new SolicitacaoRelatorioDTO();

            dto.setId(s.getId());
            dto.setDataSolicitacao(s.getDataSolicitacao());
            dto.setStatus(s.getStatus());

            dto.setCarro(montarDescricaoCarro(s));
            dto.setMotorista(s.getMotorista() != null ? s.getMotorista().getNome() : "-");
            dto.setUsuario(s.getUsuario() != null ? s.getUsuario().getNome() : "-");
            dto.setSetor(s.getSetor() != null ? s.getSetor().getNome() : "-");
            dto.setDestino(s.getDestino() != null ? s.getDestino().getNome() : "-");

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

        String base = String.join(" - ", placa, marca, modelo).replaceAll("(\\s*-\\s*)+$", "");
        return base.isBlank() ? "-" : base;
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String montarDescricaoFiltro(String filtro, LocalDate inicio, LocalDate fim) {
        List<String> partes = new ArrayList<>();

        if (filtro != null && !filtro.isBlank()) partes.add("Filtro=" + filtro);
        if (inicio != null) partes.add("Data Início=" + inicio);
        if (fim != null) partes.add("Data Fim=" + fim);

        return partes.isEmpty()
                ? "Sem filtros aplicados"
                : "Filtros: " + String.join(" | ", partes);
    }

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
            return (name != null) ? name : "Usuário não autenticado";

        } catch (Exception e) {
            log.warn("Não foi possível obter o usuário logado para o relatório", e);
            return "Usuário não identificado";
        }
    }
}
