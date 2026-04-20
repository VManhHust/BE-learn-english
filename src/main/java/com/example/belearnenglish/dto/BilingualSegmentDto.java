package com.example.belearnenglish.dto;

public record BilingualSegmentDto(
    int segmentIndex,
    double startTime,
    double endTime,
    String text,
    String translation
) {}
