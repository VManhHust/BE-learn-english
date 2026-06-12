package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.LessonPreviewDto;
import com.example.belearnenglish.dto.TopicDto;
import com.example.belearnenglish.dto.TopicLessonsResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicDto>> getAllTopics(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(topicService.getAllTopics(userId));
    }

    @GetMapping("/{slug}/lessons")
    public ResponseEntity<TopicLessonsResponse> getLessonsBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(topicService.getLessonsBySlug(slug, page, size, sortBy, userId));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof JwtClaims) {
            JwtClaims claims = (JwtClaims) authentication.getPrincipal();
            return claims.getUserId();
        }
        return null;
    }
}
