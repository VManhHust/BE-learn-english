package com.example.belearnenglish.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "learning_exercise")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningExerciseType type;

    @Column(nullable = false)
    private String title;

    @Column(name = "module_count", nullable = false)
    @Builder.Default
    private Integer moduleCount = 0;

    @Column(name = "vocabulary_level")
    private String vocabularyLevel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private LearningTopic learningTopic;

    @OneToOne(mappedBy = "learningExercise", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private YoutubeExerciseExtension youtubeExerciseExtension;

    @OneToMany(mappedBy = "learningExercise", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ExerciseModule> exerciseModules = new HashSet<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
