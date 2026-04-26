package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class YoutubeModuleExtensionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private YoutubeModuleExtensionRepository repository;

    private LearningTopic testTopic;

    @BeforeEach
    void setUp() {
        // Create test data
        testTopic = LearningTopic.builder()
                .topicName("Test Topic")
                .description("Test Description")
                .type(LearningTopicType.YOUTUBE)
                .createdAt(Instant.now())
                .build();
        entityManager.persist(testTopic);

        LearningExercise exercise = LearningExercise.builder()
                .uuid("test-uuid-123")
                .type(LearningExerciseType.YOUTUBE)
                .title("Test Exercise")
                .moduleCount(2)
                .learningTopic(testTopic)
                .createdAt(Instant.now())
                .build();
        entityManager.persist(exercise);

        // Create YoutubeModuleExtension segments
        YoutubeModuleExtension segment1 = YoutubeModuleExtension.builder()
                .timeStartMs(0)
                .timeEndMs(2500)
                .correctAnswer("Hello world")
                .vietnameseText("Xin chào thế giới")
                .build();
        entityManager.persist(segment1);

        YoutubeModuleExtension segment2 = YoutubeModuleExtension.builder()
                .timeStartMs(2500)
                .timeEndMs(5000)
                .correctAnswer("Welcome to the lesson")
                .vietnameseText(null)
                .build();
        entityManager.persist(segment2);

        // Create ExerciseModules linking to segments
        ExerciseModule module1 = ExerciseModule.builder()
                .type(ExerciseModuleType.YOUTUBE)
                .learningExercise(exercise)
                .youtubeModuleExtension(segment1)
                .build();
        entityManager.persist(module1);

        ExerciseModule module2 = ExerciseModule.builder()
                .type(ExerciseModuleType.YOUTUBE)
                .learningExercise(exercise)
                .youtubeModuleExtension(segment2)
                .build();
        entityManager.persist(module2);

        entityManager.flush();
    }

    @Test
    void findByLearningTopicIdOrderByTimeStartMsAsc_shouldReturnSegmentsSortedByTime() {
        // When
        List<YoutubeModuleExtension> segments = repository.findByLearningTopicIdOrderByTimeStartMsAsc(testTopic.getId());

        // Then
        assertThat(segments).hasSize(2);
        assertThat(segments.get(0).getTimeStartMs()).isEqualTo(0);
        assertThat(segments.get(0).getCorrectAnswer()).isEqualTo("Hello world");
        assertThat(segments.get(0).getVietnameseText()).isEqualTo("Xin chào thế giới");
        
        assertThat(segments.get(1).getTimeStartMs()).isEqualTo(2500);
        assertThat(segments.get(1).getCorrectAnswer()).isEqualTo("Welcome to the lesson");
        assertThat(segments.get(1).getVietnameseText()).isNull();
    }

    @Test
    void findByLearningTopicIdOrderByTimeStartMsAsc_whenTopicNotFound_shouldReturnEmptyList() {
        // When
        List<YoutubeModuleExtension> segments = repository.findByLearningTopicIdOrderByTimeStartMsAsc(999L);

        // Then
        assertThat(segments).isEmpty();
    }
}
