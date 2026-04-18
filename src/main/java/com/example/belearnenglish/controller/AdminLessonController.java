package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.BatchImportRequest;
import com.example.belearnenglish.dto.ImportLessonRequest;
import com.example.belearnenglish.dto.LessonDto;
import com.example.belearnenglish.service.AdminLessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lessons")
public class AdminLessonController {

    private final AdminLessonService adminLessonService;

    public AdminLessonController(AdminLessonService adminLessonService) {
        this.adminLessonService = adminLessonService;
    }

    /**
     * Import single lesson from YouTube
     * POST /api/admin/lessons/import
     * Body: {
     *   "topicId": 1,
     *   "title": "Video Title",
     *   "youtubeUrl": "https://youtube.com/watch?v=VIDEO_ID",
     *   "level": "A2",
     *   "hasDictation": true,
     *   "hasShadowing": true
     * }
     */
    @PostMapping("/import")
    public ResponseEntity<LessonDto> importLesson(@RequestBody ImportLessonRequest request) {
        try {
            LessonDto lesson = adminLessonService.importLesson(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Batch import lessons from YouTube
     * POST /api/admin/lessons/batch-import
     * Body: {
     *   "topicId": 1,
     *   "lessons": [
     *     {
     *       "title": "Video 1",
     *       "youtubeUrl": "https://youtube.com/watch?v=VIDEO_ID_1",
     *       "level": "A2",
     *       "hasDictation": true,
     *       "hasShadowing": true
     *     },
     *     ...
     *   ]
     * }
     */
    @PostMapping("/batch-import")
    public ResponseEntity<List<LessonDto>> batchImport(@RequestBody BatchImportRequest request) {
        try {
            List<LessonDto> lessons = adminLessonService.batchImport(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(lessons);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update lesson
     * PUT /api/admin/lessons/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> updateLesson(
            @PathVariable Long id,
            @RequestBody ImportLessonRequest request
    ) {
        try {
            LessonDto lesson = adminLessonService.updateLesson(id, request);
            return ResponseEntity.ok(lesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete lesson
     * DELETE /api/admin/lessons/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        adminLessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
