package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.ExerciseModuleDto;
import com.example.belearnenglish.dto.SaveModuleRequest;
import com.example.belearnenglish.repository.LearningExerciseRepository;
import com.example.belearnenglish.repository.YoutubeExerciseExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTranscriptService {

    private final YoutubeExerciseService youtubeExerciseService;
    private final LearningExerciseRepository exerciseRepository;
    private final YoutubeExerciseExtensionRepository extensionRepository;

    @Transactional
    public List<ExerciseModuleDto> saveTranscript(Long lessonId, List<SaveModuleRequest> requests) {
        // lessonId = LearningExercise.id, lấy videoId từ extension rồi delegate
        String videoId = extensionRepository.findByLearningExerciseId(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId))
                .getVideoId();
        youtubeExerciseService.saveModules(videoId, requests);
        return getTranscript(lessonId);
    }

    @Transactional(readOnly = true)
    public List<ExerciseModuleDto> getTranscript(Long lessonId) {
        if (!exerciseRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found: " + lessonId);
        }
        return youtubeExerciseService.getModules(lessonId, 0, Integer.MAX_VALUE);
    }

    @Transactional
    public void deleteTranscript(Long lessonId) {
        String videoId = extensionRepository.findByLearningExerciseId(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId))
                .getVideoId();
        youtubeExerciseService.saveModules(videoId, List.of());
    }
}
