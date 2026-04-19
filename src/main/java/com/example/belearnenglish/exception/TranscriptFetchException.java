package com.example.belearnenglish.exception;

public class TranscriptFetchException extends RuntimeException {
    public TranscriptFetchException(String message) {
        super(message);
    }

    public TranscriptFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
