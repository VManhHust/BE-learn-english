package com.example.belearnenglish.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for learning progress response.
 * Contains all progress data returned to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    
    private Long lessonId;
    
    private Map<String, SegmentResult> segmentResults;
    
    private Map<String, String> userInputs;
    
    private Integer completionPercentage;
    
    private Boolean isCompleted;
    
    private Instant completedAt;
    
    private Instant updatedAt;
}
