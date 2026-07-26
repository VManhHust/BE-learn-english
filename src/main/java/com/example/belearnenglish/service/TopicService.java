package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LessonPreviewDto;
import com.example.belearnenglish.dto.TopicDto;
import com.example.belearnenglish.dto.TopicLessonsResponse;
import com.example.belearnenglish.entity.LearningExercise;
import com.example.belearnenglish.entity.LearningTopic;
import com.example.belearnenglish.entity.YoutubeExerciseExtension;
import com.example.belearnenglish.entity.enums.PublicationStatus;
import com.example.belearnenglish.repository.LearningExerciseRepository;
import com.example.belearnenglish.repository.LearningTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final LearningTopicRepository topicRepository;
    private final LearningExerciseRepository exerciseRepository;
    private final com.example.belearnenglish.repository.ProgressRepository progressRepository;

    public List<TopicDto> getAllTopics(Long userId) {
        return topicRepository.findByStatusOrderByIdAsc(PublicationStatus.PUBLISHED).stream()
                .map(topic -> toDto(topic, userId))
                .toList();
    }

    public TopicLessonsResponse getLessonsBySlug(String slug, int page, int size, String sortBy, Long userId) {
        log.info("getLessonsBySlug: slug={}, page={}, size={}, sortBy={}, userId={}", slug, page, size, sortBy, userId);
        try {
            long topicId;
            try {
                topicId = Long.parseLong(slug);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found: " + slug);
            }
            LearningTopic topic = topicRepository.findById(topicId)
                    .filter(item -> item.getStatus() == PublicationStatus.PUBLISHED)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found: " + slug));
            log.info("Found topic: id={}, name={}", topic.getId(), topic.getTopicName());
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageable = PageRequest.of(page, size, sort);
            Page<LessonPreviewDto> result = exerciseRepository.findByLearningTopicIdAndStatus(
                            topic.getId(),
                            PublicationStatus.PUBLISHED,
                            pageable)
                    .map(exercise -> toLessonPreview(exercise, userId));
            log.info("Returning {} lessons (total={})", result.getNumberOfElements(), result.getTotalElements());
            return new TopicLessonsResponse(
                    topic.getId(),
                    topic.getTopicName(),
                    result.getTotalElements(),
                    result.getTotalPages(),
                    result.getNumber(),
                    result.getSize(),
                    result.getContent()
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getLessonsBySlug slug={}", slug, e);
            throw e;
        }
    }

    private TopicDto toDto(LearningTopic topic, Long userId) {
        long count = exerciseRepository.countByLearningTopicIdAndStatus(topic.getId(), PublicationStatus.PUBLISHED);
        List<LessonPreviewDto> previews = exerciseRepository
                .findTopByTopicIdAndStatus(topic.getId(), PublicationStatus.PUBLISHED, PageRequest.of(0, 4))
                .stream()
                .map(exercise -> toLessonPreview(exercise, userId))
                .toList();
        return new TopicDto(topic.getId(), topic.getTopicName(), String.valueOf(topic.getId()),
                topic.getDescription(), null, count, previews);
    }

    private LessonPreviewDto toLessonPreview(LearningExercise exercise, Long userId) {
        YoutubeExerciseExtension ext = exercise.getYoutubeExerciseExtension();
        String youtubeId = ext != null ? ext.getVideoId() : null;
        String thumbnail = ext != null ? ext.getThumbnailUrl() : null;
        String duration = ext != null && ext.getDurationSeconds() != null
                ? formatDuration(ext.getDurationSeconds()) : null;
        String source = ext != null && ext.getYoutubeChannel() != null
                ? ext.getYoutubeChannel().getChannelName() : null;

        // Get completion percentage for this user and lesson
        Integer completionPercentage = null;
        if (userId != null) {
            completionPercentage = progressRepository
                    .findCompletionPercentageByUserIdAndLessonId(userId, exercise.getId())
                    .orElse(null);
        }

        return new LessonPreviewDto(
                exercise.getId(),
                exercise.getTitle(),
                thumbnail,
                duration,
                exercise.getVocabularyLevel() != null ? exercise.getVocabularyLevel() : "A1",
                0L,
                source,
                true,
                false,
                youtubeId,
                youtubeId != null ? "https://www.youtube.com/watch?v=" + youtubeId : null,
                completionPercentage,
                exercise.isPremium()
        );
    }

    private String formatDuration(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }
}
