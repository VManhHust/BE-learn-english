package com.example.belearnenglish.dto;

import com.example.belearnenglish.entity.LearningExerciseType;

public record LearningExerciseDto(
        Long id,
        String uuid,
        LearningExerciseType type,
        String title,
        Integer moduleCount,
        String vocabularyLevel,
        String videoId,
        String thumbnailUrl,
        Integer durationSeconds,
        YoutubeChannelDto channel
) {}
