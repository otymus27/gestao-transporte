package com.br.sistema.entities.Dashboard;

public record DashboardMetricsDTO(
        long totalCarros,
        long totalUsuarios,
        long totalMarcas,
        long totalProprietarios
) {
}
