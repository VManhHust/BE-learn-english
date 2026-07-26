package com.example.belearnenglish.service;

import com.example.belearnenglish.entity.LearningExercise;
import com.example.belearnenglish.entity.enums.PublicationStatus;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.repository.LearningExerciseRepository;
import com.example.belearnenglish.repository.LearningTopicRepository;
import com.example.belearnenglish.repository.UserRepository;
import com.example.belearnenglish.security.JwtClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProAccessService {

    private final LearningExerciseRepository learningExerciseRepository;
    private final LearningTopicRepository learningTopicRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void assertCanAccessLesson(Long lessonId, JwtClaims claims) {
        LearningExercise lesson = learningExerciseRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        if (lesson.getStatus() != PublicationStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
        }

        if (!lesson.isPremium() || isAdmin(claims)) {
            return;
        }

        assertHasActivePro(claims);
    }

    @Transactional(readOnly = true)
    public void assertCanAccessTopicTranscripts(Long topicId, JwtClaims claims) {
        learningTopicRepository.findById(topicId)
                .filter(topic -> topic.getStatus() == PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        boolean hasPremiumLessons = learningExerciseRepository.existsByLearningTopicIdAndPremiumTrueAndStatus(
                topicId,
                PublicationStatus.PUBLISHED);
        if (!hasPremiumLessons || isAdmin(claims)) {
            return;
        }

        assertHasActivePro(claims);
    }

    private void assertHasActivePro(JwtClaims claims) {
        if (claims == null || claims.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PRO subscription required");
        }

        User user = userRepository.findById(claims.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "PRO subscription required"));

        if (!isProActive(user, Instant.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PRO subscription required");
        }
    }

    private boolean isAdmin(JwtClaims claims) {
        return claims != null && "ADMIN".equalsIgnoreCase(claims.getRole());
    }

    private boolean isProActive(User user, Instant now) {
        boolean started = user.getProStartsAt() == null || !user.getProStartsAt().isAfter(now);
        return started && user.getProExpiresAt() != null && user.getProExpiresAt().isAfter(now);
    }
}
