package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.LearningExerciseDto;
import com.example.belearnenglish.service.LessonService;
import com.example.belearnenglish.service.YoutubeExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final YoutubeExerciseService youtubeExerciseService;

    @GetMapping("/{id}")
    public ResponseEntity<LearningExerciseDto> getLesson(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<Page<LearningExerciseDto>> getLessonsByChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(youtubeExerciseService.getExercisesByChannel(channelId, page, size));
    }

    @GetMapping("/{id}/modules")
    public ResponseEntity<?> getModules(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(youtubeExerciseService.getModules(id, offset, limit));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
