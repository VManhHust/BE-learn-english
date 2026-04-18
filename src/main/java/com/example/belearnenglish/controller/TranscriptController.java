package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.TranscriptSegment;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.service.YouTubeTranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class TranscriptController {

    private final LessonRepository lessonRepository;
    private final YouTubeTranscriptService transcriptService;

    public TranscriptController(LessonRepository lessonRepository, YouTubeTranscriptService transcriptService) {
        this.lessonRepository = lessonRepository;
        this.transcriptService = transcriptService;
    }

    @GetMapping("/{id}/transcript")
    public ResponseEntity<List<TranscriptSegment>> getTranscript(@PathVariable Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id));

        if (lesson.getYoutubeId() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<TranscriptSegment> segments = transcriptService.getTranscript(lesson.getYoutubeId());
        return ResponseEntity.ok(segments);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
