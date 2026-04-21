package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.LearningExercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LearningExerciseRepository extends JpaRepository<LearningExercise, Long> {

    @Query("SELECT e FROM LearningExercise e WHERE e.learningTopic.id = :topicId ORDER BY e.createdAt DESC")
    List<LearningExercise> findTopByTopicId(Long topicId, Pageable pageable);

    Page<LearningExercise> findByLearningTopicId(Long topicId, Pageable pageable);
}
