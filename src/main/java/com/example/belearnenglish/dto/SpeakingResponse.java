package com.example.belearnenglish.dto;

import java.util.List;

public record SpeakingResponse(
        int totalSessions,
        List<RoomDto> recentRooms
) {}
