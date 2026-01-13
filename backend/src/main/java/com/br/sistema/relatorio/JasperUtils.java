package com.br.sistema.relatorio;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Map;

public class JasperUtils {

    public static JasperPrint fill(String jasperPath, Map<String, Object> params, JRDataSource dataSource) {
        try (InputStream is = new ClassPathResource(jasperPath).getInputStream()) {
            JasperReport report = (JasperReport) JRLoader.loadObject(is);
            return JasperFillManager.fillReport(report, params, dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar/preencher relatório: " + jasperPath + " - " + e.getMessage(), e);
        }
    }
}