package com.example.belearnenglish.dto;

public record TranscriptSegment(
    int index,
    double start,
    double duration,
    String text
) {}
