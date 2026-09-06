package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.LearningExercise;
import com.example.belearnenglish.entity.enums.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningExerciseRepository extends JpaRepository<LearningExercise, Long> {

    @Query("SELECT e FROM LearningExercise e WHERE e.learningTopic.id = :topicId ORDER BY e.createdAt ASC, e.id ASC")
    List<LearningExercise> findTopByTopicId(Long topicId, Pageable pageable);

    @Query("""
            SELECT e FROM LearningExercise e
            WHERE e.learningTopic.id = :topicId
              AND e.status = :status
            ORDER BY e.createdAt ASC, e.id ASC
            """)
    List<LearningExercise> findTopByTopicIdAndStatus(
            @Param("topicId") Long topicId,
            @Param("status") PublicationStatus status,
            Pageable pageable);

    Page<LearningExercise> findByLearningTopicId(Long topicId, Pageable pageable);

    Page<LearningExercise> findByLearningTopicIdAndStatus(Long topicId, PublicationStatus status, Pageable pageable);

    Page<LearningExercise> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    long countByLearningTopicId(Long topicId);

    long countByLearningTopicIdAndStatus(Long topicId, PublicationStatus status);

    boolean existsByLearningTopicId(Long topicId);

    boolean existsByLearningTopicIdAndPremiumTrue(Long topicId);

    boolean existsByLearningTopicIdAndPremiumTrueAndStatus(Long topicId, PublicationStatus status);
}
