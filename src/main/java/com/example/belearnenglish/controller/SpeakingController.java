package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.CreateRoomRequest;
import com.example.belearnenglish.dto.RoomDto;
import com.example.belearnenglish.dto.SpeakingResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.SpeakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/speaking")
@RequiredArgsConstructor
public class SpeakingController {

    private final SpeakingService speakingService;

    @GetMapping
    public ResponseEntity<SpeakingResponse> getSpeakingData() {
        JwtClaims claims = (JwtClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(speakingService.getSpeakingData(claims.userId()));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDto>> getRooms() {
        return ResponseEntity.ok(speakingService.getRooms());
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomDto> createRoom(@RequestBody CreateRoomRequest request) {
        JwtClaims claims = (JwtClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RoomDto room = speakingService.createRoom(claims.userId(), request.roomName(),
                request.maxMembers(), request.isPublic());
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }
}
