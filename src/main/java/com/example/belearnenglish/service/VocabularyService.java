package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.VocabularyResponse;
import org.springframework.stereotype.Service;

@Service
public class VocabularyService {

    public VocabularyResponse getVocabularyData(Long userId) {
        // Stub: trả về dữ liệu mặc định
        return new VocabularyResponse(0, 0, 0, 0.0);
    }
}
