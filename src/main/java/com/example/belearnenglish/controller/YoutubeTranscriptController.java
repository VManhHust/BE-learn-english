package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.youtube.YoutubeTranscriptBatchResponse;
import com.example.belearnenglish.dto.youtube.YoutubeTranscriptRequest;
import com.example.belearnenglish.service.youtube.YoutubeTranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/youtube/transcript")
@RequiredArgsConstructor
public class YoutubeTranscriptController {

    private final YoutubeTranscriptService transcriptService;

    /**
     * Downloads transcripts and metadata for one or more YouTube videos
     * 
     * @param request Request containing list of YouTube video URLs
     * @return Batch response with results for each video (success or failure)
     */
    @PostMapping("/download")
    public ResponseEntity<YoutubeTranscriptBatchResponse> downloadTranscripts(
            @Valid @RequestBody YoutubeTranscriptRequest request) {
        log.info("Received transcript download request for {} URLs", request.getUrls().size());
        
        YoutubeTranscriptBatchResponse response = transcriptService.downloadTranscripts(request);
        
        log.info("Transcript download completed: {} success, {} failures", 
                response.getSuccessCount(), response.getFailureCount());
        
        return ResponseEntity.ok(response);
    }
}
