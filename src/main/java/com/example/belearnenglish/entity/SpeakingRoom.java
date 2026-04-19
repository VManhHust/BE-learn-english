package com.example.belearnenglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "speaking_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    @Builder.Default
    private int currentMembers = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
