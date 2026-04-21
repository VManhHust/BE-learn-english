package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.LessonPreviewDto;
import com.example.belearnenglish.dto.TopicDto;
import com.example.belearnenglish.dto.TopicLessonsResponse;
import com.example.belearnenglish.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicDto>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @GetMapping("/{slug}/lessons")
    public ResponseEntity<TopicLessonsResponse> getLessonsBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(topicService.getLessonsBySlug(slug, page, size, sortBy));
    }
}
