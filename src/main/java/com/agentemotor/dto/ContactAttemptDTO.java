package com.agentemotor.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactAttemptDTO {
    private Long id;
    private LocalDateTime date;
    private String type;
    private String result;
    private String notes;
}
