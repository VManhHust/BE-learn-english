package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findTop4ByTopicIdOrderByViewCountDesc(Long topicId);
    Page<Lesson> findByTopicId(Long topicId, Pageable pageable);
    long countByTopicId(Long topicId);
}
