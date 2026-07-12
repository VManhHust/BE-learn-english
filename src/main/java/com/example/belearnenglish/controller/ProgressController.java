package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ProgressResponse;
import com.example.belearnenglish.dto.SaveProgressRequest;
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
        log.debug("Saving progress for user={}, lesson={}", userId, request.getLessonId());
        
        ProgressResponse response = progressService.saveProgress(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get learning progress for a specific lesson.
     * 
     * @param lessonId the lesson ID
     * @param claims the authenticated user claims
     * @return the progress response if found, 404 if not found
     */
    @GetMapping
    public ResponseEntity<ProgressResponse> getProgress(
            @RequestParam Long lessonId,
            @AuthenticationPrincipal JwtClaims claims) {
        
        Long userId = claims.getUserId();
        
        log.debug("Loading progress for user={}, lesson={}", userId, lessonId);
        
        return progressService.getProgress(userId, lessonId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Reset learning progress for a specific lesson.
     * Clears all segment results and user inputs.
     * 
     * @param lessonId the lesson ID
     * @param claims the authenticated user claims
     * @return 204 No Content on success
     */
    @DeleteMapping
    public ResponseEntity<Void> resetProgress(
            @RequestParam Long lessonId,
            @AuthenticationPrincipal JwtClaims claims) {
        
        Long userId = claims.getUserId();
        
        log.debug("Resetting progress for user={}, lesson={}", userId, lessonId);
        
        progressService.resetProgress(userId, lessonId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all completed exercises for the authenticated user.
     * 
     * @param claims the authenticated user claims
     * @return list of completed exercises
     */
    @GetMapping("/completed")
    public ResponseEntity<List<ProgressResponse>> getCompletedExercises(
            @AuthenticationPrincipal JwtClaims claims) {
        
        Long userId = claims.getUserId();
        log.debug("Loading all completed exercises for user={}", userId);
        List<ProgressResponse> completed = progressService.getCompletedExercises(userId);
        
        return ResponseEntity.ok(completed);
    }
}
