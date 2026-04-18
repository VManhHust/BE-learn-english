package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.LessonDto;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.service.LessonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final LessonRepository lessonRepository;

    public LessonController(LessonService lessonService, LessonRepository lessonRepository) {
        this.lessonService = lessonService;
        this.lessonRepository = lessonRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLesson(@PathVariable Long id) {
        return lessonRepository.findById(id)
                .map(l -> ResponseEntity.ok(new LessonDto(
                        l.getId(), l.getTitle(), l.getThumbnail(), l.getDuration(),
                        l.getLevel(), l.getViewCount(), l.getSource(),
                        l.getHasDictation(), l.getHasShadowing(),
                        l.getYoutubeUrl(), l.getYoutubeId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/movie-short-clip")
    public ResponseEntity<Page<LessonDto>> getMovieShortClipLessons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, 
            sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(lessonService.getLessonsByTopicSlug("movie-short-clip", pageable));
    }

    @GetMapping("/daily-conversation")
    public ResponseEntity<Page<LessonDto>> getDailyConversationLessons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, 
            sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(lessonService.getLessonsByTopicSlug("daily-english-conversation", pageable));
    }

    @GetMapping("/learning-resource")
    public ResponseEntity<Page<LessonDto>> getLearningResourceLessons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, 
            sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(lessonService.getLessonsByTopicSlug("learning-resources", pageable));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
