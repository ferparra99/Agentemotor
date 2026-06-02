package com.agentemotor.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewRequestDTO {
    private LocalDate newExpirationDate;
}
