package com.example.belearnenglish.dto;

public record RoomDto(
        long id,
        String roomName,
        int currentMembers,
        int maxMembers,
        boolean isPublic
) {}
