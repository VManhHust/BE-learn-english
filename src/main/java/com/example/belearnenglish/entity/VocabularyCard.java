package com.example.belearnenglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "vocabulary_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CardStatus status = CardStatus.LEARNING;

    @Column(nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int correctCount = 0;

    @Column
    private Instant nextReviewAt;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
