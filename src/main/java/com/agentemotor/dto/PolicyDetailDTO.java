package com.agentemotor.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolicyDetailDTO {
    private Long id;
    private String policyNumber;
    private String type;
    private String insurer;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private long daysUntilExpiry;
    private long daysOverdue;
    private String status;
    private String priority;
    private String recommendedAction;
    private int contactAttempts;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private List<ContactAttemptDTO> contactAttemptList;
}
