package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findBySlug(String slug);
}
