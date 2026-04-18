package com.example.belearnenglish.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "topics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String description;
    private String thumbnail;

    @OneToMany(mappedBy = "topic", fetch = FetchType.LAZY)
    private List<Lesson> lessons;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
