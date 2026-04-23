package com.example.belearnenglish.dto;

import com.example.belearnenglish.entity.LearningExerciseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningExerciseDto {
    private Long id;
    private String uuid;
    private LearningExerciseType type;
    private String title;
    private Integer moduleCount;
    private String vocabularyLevel;
    private String videoId;
    private String thumbnailUrl;
    private Integer durationSeconds;
    private YoutubeChannelDto channel;
}
