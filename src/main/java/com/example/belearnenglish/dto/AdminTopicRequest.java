package com.example.belearnenglish.dto;

import com.example.belearnenglish.entity.LearningTopicType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminTopicRequest {

    @NotBlank
    private String topicName;

    private String description;

    @NotNull
    private LearningTopicType type;
}
