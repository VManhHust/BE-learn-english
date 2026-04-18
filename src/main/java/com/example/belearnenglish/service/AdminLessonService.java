package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.BatchImportRequest;
import com.example.belearnenglish.dto.ImportLessonRequest;
import com.example.belearnenglish.dto.LessonDto;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.entity.Topic;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdminLessonService {

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;
    
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile(
        "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([a-zA-Z0-9_-]{11})"
    );

    public AdminLessonService(LessonRepository lessonRepository, TopicRepository topicRepository) {
        this.lessonRepository = lessonRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public LessonDto importLesson(ImportLessonRequest request) {
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + request.topicId()));

        String youtubeId = extractYoutubeId(request.youtubeUrl());
        if (youtubeId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL: " + request.youtubeUrl());
        }

        Lesson lesson = Lesson.builder()
                .topic(topic)
                .title(request.title())
                .youtubeUrl(request.youtubeUrl())
                .youtubeId(youtubeId)
                .thumbnail("https://img.youtube.com/vi/" + youtubeId + "/mqdefault.jpg")
                .level(request.level() != null ? request.level() : "A1")
                .viewCount(0L)
                .source("Youtube")
                .hasDictation(request.hasDictation() != null ? request.hasDictation() : true)
                .hasShadowing(request.hasShadowing() != null ? request.hasShadowing() : true)
                .build();

        lesson = lessonRepository.save(lesson);
        return toLessonDto(lesson);
    }

    @Transactional
    public List<LessonDto> batchImport(BatchImportRequest request) {
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + request.topicId()));

        List<LessonDto> imported = new ArrayList<>();
        
        for (BatchImportRequest.LessonImportItem item : request.lessons()) {
            String youtubeId = extractYoutubeId(item.youtubeUrl());
            if (youtubeId == null) {
                continue; // Skip invalid URLs
            }

            Lesson lesson = Lesson.builder()
                    .topic(topic)
                    .title(item.title())
                    .youtubeUrl(item.youtubeUrl())
                    .youtubeId(youtubeId)
                    .thumbnail("https://img.youtube.com/vi/" + youtubeId + "/mqdefault.jpg")
                    .level(item.level() != null ? item.level() : "A1")
                    .viewCount(0L)
                    .source("Youtube")
                    .hasDictation(item.hasDictation() != null ? item.hasDictation() : true)
                    .hasShadowing(item.hasShadowing() != null ? item.hasShadowing() : true)
                    .build();

            lesson = lessonRepository.save(lesson);
            imported.add(toLessonDto(lesson));
        }

        return imported;
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    @Transactional
    public LessonDto updateLesson(Long lessonId, ImportLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

        if (request.title() != null) {
            lesson.setTitle(request.title());
        }
        if (request.youtubeUrl() != null) {
            String youtubeId = extractYoutubeId(request.youtubeUrl());
            if (youtubeId != null) {
                lesson.setYoutubeUrl(request.youtubeUrl());
                lesson.setYoutubeId(youtubeId);
                lesson.setThumbnail("https://img.youtube.com/vi/" + youtubeId + "/mqdefault.jpg");
            }
        }
        if (request.level() != null) {
            lesson.setLevel(request.level());
        }
        if (request.hasDictation() != null) {
            lesson.setHasDictation(request.hasDictation());
        }
        if (request.hasShadowing() != null) {
            lesson.setHasShadowing(request.hasShadowing());
        }

        lesson = lessonRepository.save(lesson);
        return toLessonDto(lesson);
    }

    private String extractYoutubeId(String url) {
        if (url == null) return null;
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private LessonDto toLessonDto(Lesson l) {
        return new LessonDto(
            l.getId(),
            l.getTitle(),
            l.getThumbnail(),
            l.getDuration(),
            l.getLevel(),
            l.getViewCount(),
            l.getSource(),
            l.getHasDictation(),
            l.getHasShadowing(),
            l.getYoutubeUrl(),
            l.getYoutubeId()
        );
    }
}
