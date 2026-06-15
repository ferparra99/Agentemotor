package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Solicitud para crear o actualizar una póliza")
public class PolicyRequestDTO {

    @Schema(description = "ID de la póliza (solo para actualización)", example = "1")
    private Long id;

    @Schema(description = "Número de póliza", example = "POL-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String policyNumber;

    @Schema(description = "Tipo de póliza (AUTO, HOGAR, VIDA)", example = "AUTO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "Nombre de la aseguradora", example = "Mapfre", requiredMode = Schema.RequiredMode.REQUIRED)
    private String insurer;

    @Schema(description = "Fecha de inicio de vigencia", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @Schema(description = "Fecha de vencimiento", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expirationDate;

    @Schema(description = "Nombre del cliente", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientName;

    @Schema(description = "Teléfono del cliente", example = "555-0100")
    private String clientPhone;

    @Schema(description = "Correo electrónico del cliente", example = "juan@example.com")
    private String clientEmail;

    @Schema(description = "Notas adicionales del cliente", example = "Cliente preferencial")
    private String clientNotes;
}
