package com.br.sistema.relatorio.Services;

import com.br.sistema.entities.Carro.DTO.CarroRelatorioDTO;
import com.br.sistema.repositories.CarroRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioCarroService {

    private final CarroRepository carroRepository;

    @Transactional
    public byte[] gerarRelatorioCarrosSimples() {
        try {
            // 1. Busca dados
            List<CarroRelatorioDTO> dados = carroRepository.listarParaRelatorio();

            // 2. Carrega o JRXML do classpath
            ClassPathResource resource =
                    new ClassPathResource("reports/carros/rel_carros.jrxml");
            InputStream jrxmlStream = resource.getInputStream();

            // 3. Compila em tempo de execução
            JasperReport jasperReport =
                    JasperCompileManager.compileReport(jrxmlStream);

            // 4. Parâmetros (se precisar depois)
            Map<String, Object> params = new HashMap<>();

            // 5. DataSource
            JRBeanCollectionDataSource dataSource =
                    new JRBeanCollectionDataSource(dados);

            // 6. Preenche o relatório
            JasperPrint jasperPrint =
                    JasperFillManager.fillReport(jasperReport, params, dataSource);

            // 7. Exporta para PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            // Só pra enxergar o erro real enquanto ajusta
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar relatório de carros: " + e.getMessage(), e);
        }
    }
}
