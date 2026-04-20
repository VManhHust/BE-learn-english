package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.TranscriptSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegmentEntity, Long> {

    List<TranscriptSegmentEntity> findByLessonIdOrderBySegmentIndex(Long lessonId);

    void deleteByLessonId(Long lessonId);
}
