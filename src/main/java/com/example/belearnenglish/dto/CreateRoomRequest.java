package com.example.belearnenglish.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
        @NotBlank String roomName,
        @Min(2) @Max(5) int maxMembers,
        boolean isPublic
) {}
