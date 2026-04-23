package com.example.belearnenglish.exception.youtube;

public class InvalidYoutubeUrlException extends YoutubeTranscriptException {
    public InvalidYoutubeUrlException(String url) {
        super("Invalid YouTube URL: " + url);
    }
}
