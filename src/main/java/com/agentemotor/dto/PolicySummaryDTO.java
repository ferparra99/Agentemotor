package com.agentemotor.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolicySummaryDTO {
    private Long id;
    private String policyNumber;
    private String type;
    private String insurer;
    private String clientName;
    private String clientPhone;
    private String expirationDate;
    private long daysUntilExpiry;
    private long daysOverdue;
    private String status;
    private String priority;
    private int contactAttempts;
    private String recommendedAction;
    private String interestStatus;
    private String lastContactResult;
}
