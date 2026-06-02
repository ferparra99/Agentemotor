package com.agentemotor.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientDetailDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String notes;
    private int activePolicies;
    private int totalPolicies;
}
