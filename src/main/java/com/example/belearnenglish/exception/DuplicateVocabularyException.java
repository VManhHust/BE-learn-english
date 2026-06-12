package com.example.belearnenglish.exception;

public class DuplicateVocabularyException extends RuntimeException {
    public DuplicateVocabularyException(String word) {
        super("Word already saved in vocabulary bank: " + word);
    }
}
