package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.VocabularyDeckDetailResponse;
import com.example.belearnenglish.dto.VocabularyDecksResponse;
import com.example.belearnenglish.dto.VocabularyResponse;
import com.example.belearnenglish.dto.VocabularyReviewRequest;
import com.example.belearnenglish.dto.VocabularyQuizOptionResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<VocabularyResponse> getVocabularyData() {
        return ResponseEntity.ok(vocabularyService.getVocabularyData(getUserId()));
    }

    @GetMapping("/decks")
    public ResponseEntity<VocabularyDecksResponse> getDecks() {
        return ResponseEntity.ok(vocabularyService.getDecks(getUserId()));
    }

    @GetMapping("/decks/{deckSlug}")
    public ResponseEntity<VocabularyDeckDetailResponse> getDeckDetail(
            @PathVariable String deckSlug,
            @RequestParam(required = false) String topicSlug,
            @RequestParam(required = false) Integer cardNumber
    ) {
        return ResponseEntity.ok(vocabularyService.getDeckDetail(getUserId(), deckSlug, topicSlug, cardNumber));
    }

    @PostMapping("/words/{wordId}/review")
    public ResponseEntity<VocabularyDeckDetailResponse> reviewWord(
            @PathVariable Long wordId,
            @Valid @RequestBody VocabularyReviewRequest request
    ) {
        return ResponseEntity.ok(vocabularyService.reviewWord(getUserId(), wordId, request.rating()));
    }

    @DeleteMapping("/topics/{topicId}/progress")
    public ResponseEntity<VocabularyDeckDetailResponse> resetTopicProgress(@PathVariable Long topicId) {
        return ResponseEntity.ok(vocabularyService.resetTopicProgress(getUserId(), topicId));
    }

    @GetMapping("/topics/{topicId}/quiz-options")
    public ResponseEntity<List<VocabularyQuizOptionResponse>> getQuizOptions(
            @PathVariable Long topicId,
            @RequestParam Long excludeWordId
    ) {
        return ResponseEntity.ok(vocabularyService.getQuizOptions(topicId, excludeWordId));
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtClaims claims = (JwtClaims) auth.getPrincipal();
        return claims.getUserId();
    }
}
