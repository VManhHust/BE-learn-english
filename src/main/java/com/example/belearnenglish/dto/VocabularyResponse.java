package com.example.belearnenglish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {
    private int totalWords;
    private int learned;
    private int reviewing;
    private double accuracy;
}
