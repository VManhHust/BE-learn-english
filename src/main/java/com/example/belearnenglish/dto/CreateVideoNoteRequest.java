package com.example.belearnenglish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new video note.
 * Contains the exercise module ID and note content.
 */
public record CreateVideoNoteRequest(
    @NotNull(message = "Exercise module ID is required")
    Long exerciseModuleId,

    @NotBlank(message = "Note content cannot be empty")
    @Size(max = 5000, message = "Note content cannot exceed 5000 characters")
    String noteContent
) {}
