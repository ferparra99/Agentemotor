package com.agentemotor.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactAttemptRequestDTO {
    private Long policyId;
    private String type;
    private String result;
    private String notes;
}
