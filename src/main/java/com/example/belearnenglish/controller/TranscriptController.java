package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.TranscriptSegment;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.entity.TranscriptSegmentEntity;
import com.example.belearnenglish.exception.TranscriptFetchException;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.repository.TranscriptSegmentRepository;
import com.example.belearnenglish.service.YouTubeTranscriptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class TranscriptController {

    private final LessonRepository lessonRepository;
    private final YouTubeTranscriptService transcriptService;
    private final TranscriptSegmentRepository transcriptSegmentRepository;

    public TranscriptController(LessonRepository lessonRepository,
                                YouTubeTranscriptService transcriptService,
                                TranscriptSegmentRepository transcriptSegmentRepository) {
        this.lessonRepository = lessonRepository;
        this.transcriptService = transcriptService;
        this.transcriptSegmentRepository = transcriptSegmentRepository;
    }

    @GetMapping("/{id}/transcript")
    public ResponseEntity<?> getTranscript(@PathVariable Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id));

        // Check DB for manual transcript first
        List<TranscriptSegmentEntity> manual = transcriptSegmentRepository.findByLessonIdOrderBySegmentIndex(id);
        if (!manual.isEmpty()) {
            List<TranscriptSegment> segments = manual.stream()
                    .map(e -> new TranscriptSegment(
                            e.getSegmentIndex(),
                            e.getStartTime(),
                            e.getEndTime() - e.getStartTime(), // duration = endTime - startTime
                            e.getText()))
                    .toList();
            return ResponseEntity.ok(segments);
        }

        // Fallback to YouTube
        if (lesson.getYoutubeId() == null) {
            return ResponseEntity.ok(List.of());
        }

        try {
            List<TranscriptSegment> segments = transcriptService.getTranscript(lesson.getYoutubeId());
            return ResponseEntity.ok(segments);
        } catch (TranscriptFetchException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(e.getMessage());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
