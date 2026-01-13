package com.br.sistema.relatorio;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasperConfig {

    @PostConstruct
    public void init() {
        // Faz o Jasper compilar JRXML usando o classpath real do processo Java
        System.setProperty(
                "net.sf.jasperreports.compiler.classpath",
                System.getProperty("java.class.path")
        );

        // (opcional) log para confirmar
        // System.out.println("Jasper classpath = " + System.getProperty("net.sf.jasperreports.compiler.classpath"));
    }
}
