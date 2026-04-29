package com.example.belearnenglish.dto;

import lombok.Builder;

import java.time.Instant;

/**
 * Response DTO for video notes.
 * Contains all information about a user's note on a video segment.
 */
@Builder
public record VideoNoteResponse(
    Long id,
    String videoTitle,
    Long videoId,
    String englishText,
    String vietnameseText,
    String noteContent,
    Instant createdAt
) {}
