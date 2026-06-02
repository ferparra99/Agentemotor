package com.agentemotor.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolicyRequestDTO {
    private Long id;
    private String policyNumber;
    private String type;
    private String insurer;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String clientNotes;
}
