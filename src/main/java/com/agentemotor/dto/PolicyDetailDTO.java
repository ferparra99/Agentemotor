package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Detalle completo de una póliza, incluyendo historial de contactos")
public class PolicyDetailDTO {

    @Schema(description = "ID de la póliza", example = "1")
    private Long id;

    @Schema(description = "Número de póliza", example = "POL-001")
    private String policyNumber;

    @Schema(description = "Tipo de póliza (AUTO, HOGAR, VIDA)", example = "AUTO")
    private String type;

    @Schema(description = "Nombre de la aseguradora", example = "Mapfre")
    private String insurer;

    @Schema(description = "Fecha de inicio de vigencia", example = "2026-01-01")
    private LocalDate startDate;

    @Schema(description = "Fecha de vencimiento", example = "2026-12-31")
    private LocalDate expirationDate;

    @Schema(description = "Días restantes hasta el vencimiento (negativo si ya venció)", example = "120")
    private long daysUntilExpiry;

    @Schema(description = "Días de vencido (0 si no ha vencido)", example = "0")
    private long daysOverdue;

    @Schema(description = "Estado de la póliza (ACTIVO, VENCIDO, PERDIDO, RENOVADA)", example = "ACTIVO")
    private String status;

    @Schema(description = "Prioridad (baja, media, alta, urgente, completada, perdido)", example = "media")
    private String priority;

    @Schema(description = "Acción recomendada", example = "Gestionar renovación antes del vencimiento.")
    private String recommendedAction;

    @Schema(description = "Cantidad de intentos de contacto", example = "2")
    private int contactAttempts;

    @Schema(description = "ID del cliente asociado", example = "1")
    private Long clientId;

    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String clientName;

    @Schema(description = "Teléfono del cliente", example = "555-0100")
    private String clientPhone;

    @Schema(description = "Correo electrónico del cliente", example = "juan@example.com")
    private String clientEmail;

    @Schema(description = "Historial de intentos de contacto")
    private List<ContactAttemptDTO> contactAttemptList;
}
