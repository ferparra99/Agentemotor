package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Solicitud para registrar un intento de contacto")
public class ContactAttemptRequestDTO {

    @Schema(description = "ID de la póliza (se asigna automáticamente desde la URL)", example = "1")
    private Long policyId;

    @Schema(description = "Tipo de contacto (CALL, EMAIL, WHATSAPP)", example = "CALL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "Resultado del contacto (CONTACTED, NO_ANSWER, LEFT_MESSAGE, INTERESTED, NOT_INTERESTED)", example = "INTERESTED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String result;

    @Schema(description = "Notas adicionales sobre el contacto", example = "Cliente interesado en renovar, llamar la próxima semana")
    private String notes;
}
