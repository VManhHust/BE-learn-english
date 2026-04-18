package com.example.belearnenglish.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "lessons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private String title;

    private String thumbnail;
    
    @Column(name = "youtube_url")
    private String youtubeUrl;
    
    @Column(name = "youtube_id")
    private String youtubeId;
    
    private String duration;

    @Column(nullable = false)
    @Builder.Default
    private String level = "A1";

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Builder.Default
    private String source = "Youtube";

    @Builder.Default
    private Boolean hasDictation = true;

    @Builder.Default
    private Boolean hasShadowing = true;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
