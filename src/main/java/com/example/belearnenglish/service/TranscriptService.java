package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.TranscriptResponse;

public interface TranscriptService {
    TranscriptResponse getTranscriptByLearningTopicId(Long learningTopicId);
}
