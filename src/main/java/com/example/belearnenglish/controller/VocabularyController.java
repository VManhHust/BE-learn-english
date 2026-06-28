package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.VocabularyDeckDetailResponse;
import com.example.belearnenglish.dto.VocabularyDecksResponse;
import com.example.belearnenglish.dto.VocabularyResponse;
import com.example.belearnenglish.dto.VocabularyReviewRequest;
import com.example.belearnenglish.dto.VocabularyReviewTopicResponse;
import com.example.belearnenglish.dto.VocabularyQuizOptionResponse;
import com.example.belearnenglish.dto.VocabularyPronunciationResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.OxfordPronunciationService;
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
    private final OxfordPronunciationService oxfordPronunciationService;

    @GetMapping
    public ResponseEntity<VocabularyResponse> getVocabularyData() {
        return ResponseEntity.ok(vocabularyService.getVocabularyData(getUserId()));
    }

    @GetMapping("/decks")
    public ResponseEntity<VocabularyDecksResponse> getDecks() {
        return ResponseEntity.ok(vocabularyService.getDecks(getUserId()));
    }

    @GetMapping("/decks/{deckId}")
    public ResponseEntity<VocabularyDeckDetailResponse> getDeckDetail(
            @PathVariable Long deckId,
            @RequestParam(required = false) String topicSlug,
            @RequestParam(required = false) Integer cardNumber
    ) {
        return ResponseEntity.ok(vocabularyService.getDeckDetail(getUserId(), deckId, topicSlug, cardNumber));
    }

    @PostMapping("/words/{wordId}/review")
    public ResponseEntity<VocabularyDeckDetailResponse> reviewWord(
            @PathVariable Long wordId,
            @Valid @RequestBody VocabularyReviewRequest request
    ) {
        return ResponseEntity.ok(vocabularyService.reviewWord(getUserId(), wordId, request.rating()));
    }

    @DeleteMapping("/topics/{topicId}/progress")
    public ResponseEntity<VocabularyDeckDetailResponse> resetTopicProgress(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "false") boolean shuffle
    ) {
        return ResponseEntity.ok(vocabularyService.resetTopicProgress(getUserId(), topicId, shuffle));
    }

    @PostMapping("/topics/{topicId}/shuffle")
    public ResponseEntity<VocabularyDeckDetailResponse> shuffleRemainingTopicWords(@PathVariable Long topicId) {
        return ResponseEntity.ok(vocabularyService.shuffleRemainingTopicWords(getUserId(), topicId));
    }

    @GetMapping("/topics/{topicId}/quiz-options")
    public ResponseEntity<List<VocabularyQuizOptionResponse>> getQuizOptions(
            @PathVariable Long topicId,
            @RequestParam Long excludeWordId
    ) {
        return ResponseEntity.ok(vocabularyService.getQuizOptions(topicId, excludeWordId));
    }

    @GetMapping("/topics/{topicId}/words")
    public ResponseEntity<List<VocabularyDeckDetailResponse.WordCardDto>> getTopicWords(@PathVariable Long topicId) {
        return ResponseEntity.ok(vocabularyService.getTopicWords(getUserId(), topicId));
    }

    @GetMapping("/review")
    public ResponseEntity<List<VocabularyDeckDetailResponse.WordCardDto>> getReviewWords(
            @RequestParam(required = false) Long topicId
    ) {
        return ResponseEntity.ok(vocabularyService.getReviewWords(getUserId(), topicId));
    }

    @GetMapping("/review/topics")
    public ResponseEntity<List<VocabularyReviewTopicResponse>> getReviewTopics() {
        return ResponseEntity.ok(vocabularyService.getReviewTopics(getUserId()));
    }

    @GetMapping("/words")
    public ResponseEntity<List<VocabularyDeckDetailResponse.WordCardDto>> getWords() {
        return ResponseEntity.ok(vocabularyService.getWords(getUserId()));
    }

    @GetMapping("/pronunciation")
    public ResponseEntity<VocabularyPronunciationResponse> getPronunciation(
            @RequestParam String word,
            @RequestParam(defaultValue = "US") String accent
    ) {
        return ResponseEntity.ok(oxfordPronunciationService.getPronunciation(word, accent));
    }

    @GetMapping("/review/options")
    public ResponseEntity<List<VocabularyQuizOptionResponse>> getReviewQuizOptions(
            @RequestParam Long excludeWordId,
            @RequestParam(required = false) Long topicId
    ) {
        return ResponseEntity.ok(vocabularyService.getReviewQuizOptions(excludeWordId, topicId));
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtClaims claims = (JwtClaims) auth.getPrincipal();
        return claims.getUserId();
    }
}
