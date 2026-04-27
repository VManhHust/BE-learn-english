package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.SaveVocabularyRequest;
import com.example.belearnenglish.dto.VocabularyBankEntryResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.VocabularyBankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vocabulary-bank")
@RequiredArgsConstructor
@Slf4j
public class VocabularyBankController {

    private final VocabularyBankService vocabularyBankService;

    @PostMapping
    public ResponseEntity<VocabularyBankEntryResponse> save(
            @Valid @RequestBody SaveVocabularyRequest request,
            @AuthenticationPrincipal JwtClaims claims) {

        Long userId = claims.getUserId();
        log.debug("Saving word '{}' to vocabulary bank for user={}", request.getWord(), userId);
        VocabularyBankEntryResponse response = vocabularyBankService.save(userId, request.getWord());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<VocabularyBankEntryResponse>> list(
            @AuthenticationPrincipal JwtClaims claims,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = claims.getUserId();
        Page<VocabularyBankEntryResponse> result = vocabularyBankService.findByUser(
                userId, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtClaims claims) {

        Long userId = claims.getUserId();
        log.debug("Deleting vocabulary entry id={} for user={}", id, userId);
        vocabularyBankService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
