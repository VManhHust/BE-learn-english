package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.TranscriptResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.ProAccessService;
import com.example.belearnenglish.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transcript")
@CrossOrigin(origins = "${allowed.origin:http://localhost:3000}")
@RequiredArgsConstructor
public class TranscriptApiController {

    private final TranscriptService transcriptService;
    private final ProAccessService proAccessService;

    /**
     * Get transcript segments for a learning topic
     * 
     * @param learningTopicId ID of the learning topic
     * @return TranscriptResponse with segments sorted by startTimeMs
     * @throws TranscriptNotFoundException if learning topic does not exist (404)
     */
    @GetMapping("/{learningTopicId}")
    public ResponseEntity<TranscriptResponse> getTranscript(
            @PathVariable Long learningTopicId,
            @AuthenticationPrincipal JwtClaims claims) {
        proAccessService.assertCanAccessTopicTranscripts(learningTopicId, claims);
        TranscriptResponse response = transcriptService.getTranscriptByLearningTopicId(learningTopicId);
        return ResponseEntity.ok(response);
    }
}
