package com.example.belearnenglish.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transcript_segments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptSegmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "segment_index", nullable = false)
    private int segmentIndex;

    @Column(name = "start_time", nullable = false)
    private double startTime;

    @Column(name = "end_time", nullable = false)
    private double endTime;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String translation;
}
