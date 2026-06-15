package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Estadísticas agregadas del dashboard para un asesor")
public class DashboardStatsDTO {

    @Schema(description = "Total de pólizas activas", example = "25")
    private long totalActive;

    @Schema(description = "Pólizas que vencen esta semana", example = "3")
    private long expiringThisWeek;

    @Schema(description = "Pólizas que vencen este mes", example = "8")
    private long expiringThisMonth;

    @Schema(description = "Pólizas vencidas hace menos de 30 días", example = "5")
    private long expiredWithin30Days;

    @Schema(description = "Pólizas vencidas hace más de 30 días", example = "2")
    private long expiredBeyond30Days;

    @Schema(description = "Total de pólizas renovadas", example = "12")
    private long totalRenewed;
}
