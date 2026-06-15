package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Resumen de póliza para listado en tabla")
public class PolicySummaryDTO {

    @Schema(description = "ID de la póliza", example = "1")
    private Long id;

    @Schema(description = "Número de póliza", example = "POL-001")
    private String policyNumber;

    @Schema(description = "Tipo de póliza (AUTO, HOGAR, VIDA)", example = "AUTO")
    private String type;

    @Schema(description = "Nombre de la aseguradora", example = "Mapfre")
    private String insurer;

    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String clientName;

    @Schema(description = "Teléfono del cliente", example = "555-0100")
    private String clientPhone;

    @Schema(description = "Fecha de vencimiento (formato ISO)", example = "2026-12-31")
    private String expirationDate;

    @Schema(description = "Días restantes hasta el vencimiento (negativo si ya venció)", example = "120")
    private long daysUntilExpiry;

    @Schema(description = "Días de vencido (0 si no ha vencido)", example = "0")
    private long daysOverdue;

    @Schema(description = "Estado de la póliza (ACTIVO, VENCIDO, PERDIDO, RENOVADA)", example = "ACTIVO")
    private String status;

    @Schema(description = "Prioridad (baja, media, alta, urgente, completada, perdido)", example = "media")
    private String priority;

    @Schema(description = "Cantidad de intentos de contacto registrados", example = "2")
    private int contactAttempts;

    @Schema(description = "Acción recomendada para el asesor", example = "Gestionar renovación antes del vencimiento.")
    private String recommendedAction;

    @Schema(description = "Estado de interés del cliente", example = "Interesado")
    private String interestStatus;

    @Schema(description = "Último resultado de contacto registrado", example = "No contactado")
    private String lastContactResult;
}
