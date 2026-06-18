package com.agentemotor.controller.api;

import com.agentemotor.dto.DashboardStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dashboard", description = "Estadísticas del dashboard — indicadores agregados para la vista principal")
public interface StatsApi {

    @Operation(
        summary = "Obtener estadísticas del dashboard",
        description = """
            Retorta las estadísticas agregadas del dashboard para un asesor:
            - Total de pólizas activas
            - Pólizas por vencer esta semana
            - Pólizas por vencer este mes
            - Pólizas vencidas hace menos de 30 días
            - Pólizas vencidas hace más de 30 días
            - Total de pólizas renovadas

            **Ejemplo de consumo:**
            ```
            GET /api/dashboard/stats?advisorId=1
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos (advisorId requerido)")
    })
    @GetMapping("/stats")
    ResponseEntity<DashboardStatsDTO> getByAdvisor(
            @Parameter(description = "ID del asesor", required = true, example = "1")
            @RequestParam Long advisorId);
}
