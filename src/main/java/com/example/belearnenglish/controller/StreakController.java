package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.StreakResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/streak")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    @GetMapping
    public ResponseEntity<StreakResponse> getStatus(
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(streakService.getStatus(claims.getUserId()));
    }

    @PostMapping("/check-in")
    public ResponseEntity<StreakResponse> checkIn(
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(streakService.checkIn(claims.getUserId()));
    }
}
