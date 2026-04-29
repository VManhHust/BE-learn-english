package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.CreateVideoNoteRequest;
import com.example.belearnenglish.dto.VideoNoteResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.VideoNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user video notes.
 * Provides endpoints for creating and retrieving notes linked to video segments.
 */
@RestController
@RequestMapping("/api/v1/video-notes")
@RequiredArgsConstructor
@Slf4j
public class VideoNoteController {

    private final VideoNoteService videoNoteService;

    /**
     * Create a new video note.
     * 
     * @param request the note creation request containing exercise module ID and note content
     * @param claims the authenticated user claims from JWT token
     * @return the created video note response with 201 Created status
     */
    @PostMapping
    public ResponseEntity<VideoNoteResponse> createNote(
            @Valid @RequestBody CreateVideoNoteRequest request,
            @AuthenticationPrincipal JwtClaims claims) {

        Long userId = claims.getUserId();
        log.debug("Creating video note for user={}, exerciseModuleId={}", userId, request.exerciseModuleId());

        VideoNoteResponse response = videoNoteService.createNote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get paginated list of video notes for the authenticated user.
     * Notes are sorted by creation time in descending order (newest first).
     * 
     * @param claims the authenticated user claims from JWT token
     * @param page the page number (0-indexed, default: 0)
     * @param size the page size (default: 20)
     * @return page of video note responses with 200 OK status
     */
    @GetMapping
    public ResponseEntity<Page<VideoNoteResponse>> listNotes(
            @AuthenticationPrincipal JwtClaims claims,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = claims.getUserId();
        log.debug("Fetching video notes for user={}, page={}, size={}", userId, page, size);

        Page<VideoNoteResponse> notes = videoNoteService.getUserNotes(
            userId, PageRequest.of(page, size));
        return ResponseEntity.ok(notes);
    }

    /**
     * Get video note by exercise module ID for the authenticated user.
     * Returns the note if it exists, or 404 if not found.
     * 
     * @param exerciseModuleId the exercise module ID
     * @param claims the authenticated user claims from JWT token
     * @return the video note response with 200 OK status, or 404 if not found
     */
    @GetMapping("/by-module/{exerciseModuleId}")
    public ResponseEntity<VideoNoteResponse> getNoteByModule(
            @PathVariable Long exerciseModuleId,
            @AuthenticationPrincipal JwtClaims claims) {

        Long userId = claims.getUserId();
        log.debug("Fetching video note for user={}, exerciseModuleId={}", userId, exerciseModuleId);

        return videoNoteService.getNoteByUserAndModule(userId, exerciseModuleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing video note.
     * 
     * @param noteId the ID of the note to update
     * @param request the note update request containing new note content
     * @param claims the authenticated user claims from JWT token
     * @return the updated video note response with 200 OK status
     */
    @PutMapping("/{noteId}")
    public ResponseEntity<VideoNoteResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody CreateVideoNoteRequest request,
            @AuthenticationPrincipal JwtClaims claims) {

        Long userId = claims.getUserId();
        log.debug("Updating video note id={} for user={}", noteId, userId);

        VideoNoteResponse response = videoNoteService.updateNote(userId, noteId, request.noteContent());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a video note.
     * 
     * @param noteId the ID of the note to delete
     * @param claims the authenticated user claims from JWT token
     * @return 204 No Content status on successful deletion
     */
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal JwtClaims claims) {

        Long userId = claims.getUserId();
        log.debug("Deleting video note id={} for user={}", noteId, userId);

        videoNoteService.deleteNote(userId, noteId);
        return ResponseEntity.noContent().build();
    }
}
