package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ProgressResponse;
import com.example.belearnenglish.dto.SaveProgressRequest;
import com.example.belearnenglish.entity.DictationSubmode;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user learning progress.
 * Provides endpoints for saving, loading, and resetting progress.
 */
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {
    
    private final ProgressService progressService;
    
    /**
     * Save or update learning progress.
     * 
     * @param request the progress data to save
     * @param claims the authenticated user claims
     * @return the saved progress response
     */
    @PostMapping
    public ResponseEntity<ProgressResponse> saveProgress(
            @Valid @RequestBody SaveProgressRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        
        Long userId = claims.getUserId();
        log.debug("Saving progress for user={}, lesson={}, submode={}", 
                userId, request.getLessonId(), request.getSubmode());
        
        ProgressResponse response = progressService.saveProgress(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get learning progress for a specific lesson and submode.
     * 
     * @param lessonId the lesson ID
     * @param submode the dictation submode (full or fill-blank)
     * @param claims the authenticated user claims
     * @return the progress response if found, 404 if not found
     */
    @GetMapping
    public ResponseEntity<ProgressResponse> getProgress(
            @RequestParam Long lessonId,
            @RequestParam String submode,
            @AuthenticationPrincipal JwtClaims claims) {
        
        Long userId = claims.getUserId();
        DictationSubmode submodeEnum = DictationSubmode.fromJson(submode);
        
        log.debug("Loading progress for user={}, lesson={}, submode={}", 
                userId, lessonId, submodeEnum);
        
        return progressService.getProgress(userId, lessonId, submodeEnum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Reset learning progress for a specific lesson and submode.
     * Clears all segment results and user inputs.
     * 
     * @param lessonId the lesson ID
     * @param submode the dictation submode (full or fill-blank)
     * @param claims the authenticated user claims
     * @return 204 No Content on success
     */
    @DeleteMapping
    public ResponseEntity<Void> resetProgress(
            @RequestParam Long lessonId,
            @RequestParam String submode,
            @AuthenticationPrincipal JwtClaims claims) {
        
        Long userId = claims.getUserId();
        DictationSubmode submodeEnum = DictationSubmode.fromJson(submode);
        
        log.debug("Resetting progress for user={}, lesson={}, submode={}", 
                userId, lessonId, submodeEnum);
        
        progressService.resetProgress(userId, lessonId, submodeEnum);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all completed exercises for the authenticated user.
     * Optionally filter by submode.
     * 
     * @param claims the authenticated user claims
     * @param submode optional submode filter (full or fill-blank)
     * @return list of completed exercises
     */
    @GetMapping("/completed")
    public ResponseEntity<List<ProgressResponse>> getCompletedExercises(
            @AuthenticationPrincipal JwtClaims claims,
            @RequestParam(required = false) String submode) {
        
        Long userId = claims.getUserId();
        List<ProgressResponse> completed;
        
        if (submode != null) {
            DictationSubmode submodeEnum = DictationSubmode.fromJson(submode);
            log.debug("Loading completed exercises for user={}, submode={}", userId, submodeEnum);
            completed = progressService.getCompletedExercises(userId, submodeEnum);
        } else {
            log.debug("Loading all completed exercises for user={}", userId);
            completed = progressService.getCompletedExercises(userId);
        }
        
        return ResponseEntity.ok(completed);
    }
}
