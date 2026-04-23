package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.VocabularyResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<VocabularyResponse> getVocabularyData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtClaims claims = (JwtClaims) auth.getPrincipal();
        return ResponseEntity.ok(vocabularyService.getVocabularyData(claims.getUserId()));
    }
}
