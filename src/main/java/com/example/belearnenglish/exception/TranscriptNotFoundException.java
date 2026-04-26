package com.example.belearnenglish.exception;

public class TranscriptNotFoundException extends RuntimeException {
    public TranscriptNotFoundException(Long learningTopicId) {
        super("Learning topic not found: " + learningTopicId);
    }
}
