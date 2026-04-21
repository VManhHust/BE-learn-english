package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LearningExerciseDto;
import com.example.belearnenglish.entity.YoutubeExerciseExtension;
import com.example.belearnenglish.repository.LearningTopicRepository;
import com.example.belearnenglish.repository.YoutubeExerciseExtensionRepository;
import com.example.belearnenglish.entity.LearningTopicType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final YoutubeExerciseExtensionRepository extensionRepository;
    private final LearningTopicRepository topicRepository;
    private final YoutubeExerciseService youtubeExerciseService;

    public Page<LearningExerciseDto> getLessonsByTopicType(LearningTopicType type, Pageable pageable) {
        // Hiện tại chỉ hỗ trợ YOUTUBE — lấy tất cả exercise theo channel
        // Trả về toàn bộ exercises thuộc topic YOUTUBE
        return extensionRepository.findAll(pageable)
                .map(ext -> youtubeExerciseService.toExerciseDtoPublic(ext.getLearningExercise(), ext));
    }

    public LearningExerciseDto getLessonById(Long id) {
        return youtubeExerciseService.getExerciseById(id);
    }
}
