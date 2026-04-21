package com.example.belearnenglish.dto;

public record ExerciseModuleDto(
        Long id,
        Integer timeStartMs,
        Integer timeEndMs,
        String content
) {}
