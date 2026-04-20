package com.example.belearnenglish.dto;

public record TranscriptSegmentRequest(
    int segmentIndex,
    double startTime,
    double endTime,
    String text,
    String translation
) {}
