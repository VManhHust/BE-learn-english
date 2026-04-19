package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.VocabularyResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.VocabularyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping
    public ResponseEntity<VocabularyResponse> getVocabularyData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtClaims claims = (JwtClaims) auth.getPrincipal();
        return ResponseEntity.ok(vocabularyService.getVocabularyData(claims.userId()));
    }
}
