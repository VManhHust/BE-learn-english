package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LeaderboardEntryDto;
import com.example.belearnenglish.dto.VocabularyResponse;
import com.example.belearnenglish.dto.VocabularyStatsDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularyService {

    public VocabularyResponse getVocabularyData(Long userId) {
        // Stub phase: trả về dữ liệu mặc định (all zeros)
        VocabularyStatsDto stats = new VocabularyStatsDto(0, 0, 0, 0.0);
        List<LeaderboardEntryDto> leaderboard = List.of();
        return new VocabularyResponse(stats, leaderboard);
    }
}
