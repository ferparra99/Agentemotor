package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Detalle de un cliente con conteo de pólizas")
public class ClientDetailDTO {

    @Schema(description = "ID del cliente", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del cliente", example = "Juan Pérez")
    private String name;

    @Schema(description = "Teléfono de contacto", example = "555-0100")
    private String phone;

    @Schema(description = "Correo electrónico", example = "juan@example.com")
    private String email;

    @Schema(description = "Notas adicionales", example = "Cliente preferencial desde 2020")
    private String notes;

    @Schema(description = "Cantidad de pólizas activas", example = "2")
    private int activePolicies;

    @Schema(description = "Total de pólizas (activas + vencidas + renovadas)", example = "3")
    private int totalPolicies;
}
