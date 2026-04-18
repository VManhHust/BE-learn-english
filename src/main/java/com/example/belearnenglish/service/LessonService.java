package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LessonDto;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.entity.Topic;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.repository.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;

    public LessonService(LessonRepository lessonRepository, TopicRepository topicRepository) {
        this.lessonRepository = lessonRepository;
        this.topicRepository = topicRepository;
    }

    public Page<LessonDto> getLessonsByTopicSlug(String slug, Pageable pageable) {
        Topic topic = topicRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with slug: " + slug));
        
        return lessonRepository.findByTopicId(topic.getId(), pageable)
                .map(this::toLessonDto);
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
