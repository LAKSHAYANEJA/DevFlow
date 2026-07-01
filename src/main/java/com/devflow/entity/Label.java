package com.devflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "labels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private String color = "#6B7280";

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
    
}
