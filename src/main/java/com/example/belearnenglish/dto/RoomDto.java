package com.example.belearnenglish.dto;

public record RoomDto(
        Long id,
        String roomName,
        int maxMembers,
        boolean isPublic,
        int currentMembers
) {}
