package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ExerciseModuleDto;
import com.example.belearnenglish.service.YoutubeExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class TranscriptController {

    private final YoutubeExerciseService youtubeExerciseService;

    /** GET /api/lessons/{id}/transcript */
    @GetMapping("/{id}/transcript")
    public ResponseEntity<List<ExerciseModuleDto>> getTranscript(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(youtubeExerciseService.getModules(id, offset, limit));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
