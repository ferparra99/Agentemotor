package com.agentemotor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column
    private String email;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id", nullable = false)
    private Advisor advisor;
}
