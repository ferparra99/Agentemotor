package com.agentemotor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_attempts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactAttemptType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactAttemptResult result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
}
