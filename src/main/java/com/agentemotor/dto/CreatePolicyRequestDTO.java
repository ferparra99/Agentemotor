package com.agentemotor.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreatePolicyRequestDTO {
    private String policyNumber;
    private String type;
    private String insurer;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private Long clientId;
}
