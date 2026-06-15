package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Intento de contacto registrado en una póliza")
public class ContactAttemptDTO {

    @Schema(description = "ID del intento de contacto", example = "1")
    private Long id;

    @Schema(description = "Fecha y hora del contacto", example = "2026-06-15T10:30:00")
    private LocalDateTime date;

    @Schema(description = "Tipo de contacto (CALL, EMAIL, WHATSAPP)", example = "CALL")
    private String type;

    @Schema(description = "Resultado (CONTACTED, NO_ANSWER, LEFT_MESSAGE, INTERESTED, NOT_INTERESTED)", example = "INTERESTED")
    private String result;

    @Schema(description = "Notas del contacto", example = "Cliente interesado en renovar")
    private String notes;
}
