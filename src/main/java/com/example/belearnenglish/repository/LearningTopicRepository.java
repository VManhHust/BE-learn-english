package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.LearningTopic;
import com.example.belearnenglish.entity.LearningTopicType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LearningTopicRepository extends JpaRepository<LearningTopic, Long> {
    @Query("SELECT t FROM LearningTopic t WHERE t.type = :type ORDER BY t.id ASC LIMIT 1")
    Optional<LearningTopic> findByType(LearningTopicType type);

    // Tìm theo id (slug là id của topic)
    @Query(value = "SELECT * FROM learning_topic WHERE id = :id", nativeQuery = true)
    Optional<LearningTopic> findBySlug(Long id);
}
