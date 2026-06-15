package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Solicitud para renovar una póliza con nueva fecha de vencimiento")
public class RenewRequestDTO {

    @Schema(description = "Nueva fecha de vencimiento de la póliza renovada", example = "2027-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate newExpirationDate;
}
