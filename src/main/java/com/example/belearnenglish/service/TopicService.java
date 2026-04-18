package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LessonDto;
import com.example.belearnenglish.dto.TopicDto;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.entity.Topic;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;

    public TopicService(TopicRepository topicRepository, LessonRepository lessonRepository) {
        this.topicRepository = topicRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private TopicDto toDto(Topic topic) {
        long count = lessonRepository.countByTopicId(topic.getId());
        List<LessonDto> previews = lessonRepository
                .findTop4ByTopicIdOrderByViewCountDesc(topic.getId())
                .stream().map(this::toLessonDto).toList();
        return new TopicDto(topic.getId(), topic.getName(), topic.getSlug(),
                topic.getDescription(), topic.getThumbnail(), count, previews);
    }

    private LessonDto toLessonDto(Lesson l) {
        return new LessonDto(l.getId(), l.getTitle(), l.getThumbnail(),
                l.getDuration(), l.getLevel(), l.getViewCount(),
                l.getSource(), l.getHasDictation(), l.getHasShadowing(),
                l.getYoutubeUrl(), l.getYoutubeId());
    }
}
