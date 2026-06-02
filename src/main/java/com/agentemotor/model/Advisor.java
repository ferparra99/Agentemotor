package com.agentemotor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "advisors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Advisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String phone;
}
