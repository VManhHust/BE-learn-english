package com.example.belearnenglish.dto;

public record TranscriptSegmentResponse(
    Long id,
    Long lessonId,
    int segmentIndex,
    double startTime,
    double endTime,
    String text,
    String translation
) {}
