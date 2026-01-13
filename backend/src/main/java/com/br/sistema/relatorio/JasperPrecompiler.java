package com.br.sistema.relatorio;

import net.sf.jasperreports.engine.JasperCompileManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class JasperPrecompiler {

    public static void main(String[] args) throws Exception {
        // args[0] = src/main/resources
        // args[1] = target/classes
        Path resourcesDir = Paths.get(args[0]).toAbsolutePath().normalize();
        Path outputDir = Paths.get(args[1]).toAbsolutePath().normalize();

        Path reportsRoot = resourcesDir.resolve("reports");
        if (!Files.exists(reportsRoot)) {
            System.out.println("[JasperPrecompiler] Nenhuma pasta 'reports' encontrada em: " + reportsRoot);
            return;
        }

        try (Stream<Path> files = Files.walk(reportsRoot)) {
            files.filter(p -> p.toString().endsWith(".jrxml"))
                    .forEach(jrxml -> {
                        try {
                            Path relative = resourcesDir.relativize(jrxml);
                            Path jasperOut = outputDir.resolve(relative.toString().replace(".jrxml", ".jasper"));

                            Files.createDirectories(jasperOut.getParent());

                            System.out.println("[JasperPrecompiler] Compilando: " + relative);
                            JasperCompileManager.compileReportToFile(
                                    jrxml.toString(),
                                    jasperOut.toString()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("Falha compilando JRXML: " + jrxml + " -> " + e.getMessage(), e);
                        }
                    });
        }
    }
}
