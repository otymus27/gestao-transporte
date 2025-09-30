package com.br.sistema.controllers;

import com.br.sistema.entities.Dashboard.DTO.DashboardDTO;
import com.br.sistema.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<DashboardDTO> getDashboard() {
        try {
            DashboardDTO dashboard = dashboardService.getDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Erro ao gerar estatísticas do dashboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
