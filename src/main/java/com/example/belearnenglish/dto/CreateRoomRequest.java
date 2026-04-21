package com.example.belearnenglish.dto;

public record CreateRoomRequest(
        String roomName,
        int maxMembers,
        boolean isPublic
) {}
