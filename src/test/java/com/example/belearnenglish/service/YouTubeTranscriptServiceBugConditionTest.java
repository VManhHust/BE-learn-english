package com.example.belearnenglish.service;

import com.example.belearnenglish.controller.TranscriptController;
import com.example.belearnenglish.dto.TranscriptSegment;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Bug Condition Exploration Test — Property 1
 *
 * Validates: Requirements 1.2, 1.4
 *
 * CRITICAL: These tests MUST FAIL on unfixed code.
 * Failure confirms the bug exists:
 *   - getTranscript("dQw4w9WgXcQ") returns [] when HTML lacks "captionTracks"
 *     and fallback timedtext returns 403.
 *   - TranscriptController returns HTTP 200 with [] when service swallows exception.
 *
 * Counterexample documented:
 *   getTranscript("dQw4w9WgXcQ") returns [] when:
 *     1. fetchUrl(pageUrl) returns HTML without "captionTracks" (bot detection)
 *     2. fetchUrl(captionUrl) returns HTTP 403 (fallback failure)
 */
class YouTubeTranscriptServiceBugConditionTest {

    private static final String YOUTUBE_ID = "dQw4w9WgXcQ";

    /**
     * Property 1 — Bug Condition Test A:
     * When fetchUrl(pageUrl) returns HTML without "captionTracks" (bot detection),
     * getTranscript() SHOULD return a non-empty list.
     *
     * EXPECTED ON UNFIXED CODE: FAILS — service returns [] instead of non-empty list.
     * Counterexample: getTranscript("dQw4w9WgXcQ") = []
     */
    @Test
    void getTranscript_whenPageHtmlLacksCaptionTracks_shouldReturnNonEmptyList() throws Exception {
        // HTML that simulates bot detection — no "captionTracks" key present
        String botDetectionHtml = "<html><body><p>Please verify you are human</p></body></html>";

        // Valid XML transcript that would be returned if the fallback URL worked
        String validTranscriptXml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<transcript>"
                + "<text start=\"0.0\" dur=\"2.5\">Never gonna give you up</text>"
                + "<text start=\"2.5\" dur=\"2.0\">Never gonna let you down</text>"
                + "</transcript>";

        YouTubeTranscriptService service = new YouTubeTranscriptService();

        try (MockedConstruction<URL> mockedUrl = Mockito.mockConstruction(URL.class, (mockUrl, context) -> {
            HttpURLConnection mockConn = mock(HttpURLConnection.class);
            when(mockUrl.openConnection()).thenReturn(mockConn);

            String requestedUrl = (String) context.arguments().get(0);

            if (requestedUrl.contains("youtube.com/watch")) {
                // Simulate bot detection: return HTML without captionTracks
                InputStream htmlStream = new ByteArrayInputStream(botDetectionHtml.getBytes("UTF-8"));
                when(mockConn.getInputStream()).thenReturn(htmlStream);
                when(mockConn.getResponseCode()).thenReturn(200);
            } else {
                // Fallback timedtext URL: return valid XML
                InputStream xmlStream = new ByteArrayInputStream(validTranscriptXml.getBytes("UTF-8"));
                when(mockConn.getInputStream()).thenReturn(xmlStream);
                when(mockConn.getResponseCode()).thenReturn(200);
            }
        })) {
            List<TranscriptSegment> result = service.getTranscript(YOUTUBE_ID);

            // ASSERT: should return non-empty list
            // ON UNFIXED CODE: this FAILS — service returns [] because extractCaptionUrl
            // returns null when HTML lacks "captionTracks", and falls back to timedtext URL
            // which may also fail. Bug confirmed when this assertion fails.
            assertThat(result)
                    .as("getTranscript(\"%s\") should return non-empty list when HTML lacks captionTracks "
                            + "— FAILS on unfixed code, confirming bug exists", YOUTUBE_ID)
                    .isNotEmpty();
        }
    }

    /**
     * Property 1 — Bug Condition Test B:
     * When fetchUrl(captionUrl) returns HTTP 403 (fallback timedtext API blocked),
     * getTranscript() SHOULD return a non-empty list.
     *
     * EXPECTED ON UNFIXED CODE: FAILS — service returns [] instead of non-empty list.
     * Counterexample: getTranscript("dQw4w9WgXcQ") = [] when fallback returns 403
     */
    @Test
    void getTranscript_whenFallbackTimedtextReturns403_shouldReturnNonEmptyList() throws Exception {
        // HTML with captionTracks but the extracted URL will return 403
        String htmlWithCaptionTracks = "{\"captionTracks\":[{\"baseUrl\":\"https://www.youtube.com/api/timedtext?v="
                + YOUTUBE_ID + "\\u0026lang=en\"}]}";

        YouTubeTranscriptService service = new YouTubeTranscriptService();

        try (MockedConstruction<URL> mockedUrl = Mockito.mockConstruction(URL.class, (mockUrl, context) -> {
            HttpURLConnection mockConn = mock(HttpURLConnection.class);
            when(mockUrl.openConnection()).thenReturn(mockConn);

            String requestedUrl = (String) context.arguments().get(0);

            if (requestedUrl.contains("youtube.com/watch")) {
                // Return HTML with captionTracks pointing to a URL
                InputStream htmlStream = new ByteArrayInputStream(htmlWithCaptionTracks.getBytes("UTF-8"));
                when(mockConn.getInputStream()).thenReturn(htmlStream);
                when(mockConn.getResponseCode()).thenReturn(200);
            } else {
                // Simulate HTTP 403 on caption URL fetch
                when(mockConn.getResponseCode()).thenReturn(403);
                when(mockConn.getInputStream()).thenThrow(new java.io.IOException("Server returned HTTP response code: 403"));
            }
        })) {
            List<TranscriptSegment> result = service.getTranscript(YOUTUBE_ID);

            // ASSERT: should return non-empty list
            // ON UNFIXED CODE: this FAILS — service catches IOException and returns []
            // Bug confirmed: silent exception swallowing (Requirement 1.4)
            assertThat(result)
                    .as("getTranscript(\"%s\") should return non-empty list when fallback returns 403 "
                            + "— FAILS on unfixed code, confirming bug exists", YOUTUBE_ID)
                    .isNotEmpty();
        }
    }

    /**
     * Property 1 — Bug Condition Test C (Controller):
     * TranscriptController returns HTTP 200 with [] when service swallows exception.
     *
     * EXPECTED ON UNFIXED CODE: PASSES — confirms missing error handling (Requirement 1.4).
     * This test documents the defect: controller cannot distinguish error from "no transcript".
     */
    @Test
    void transcriptController_whenServiceSwallowsException_shouldReturnHttp503NotHttp200() {
        // Arrange: mock service that swallows exception and returns []
        YouTubeTranscriptService mockService = mock(YouTubeTranscriptService.class);
        when(mockService.getTranscript(any())).thenReturn(List.of()); // simulates swallowed exception

        LessonRepository mockRepo = mock(LessonRepository.class);
        Lesson lesson = new Lesson();
        lesson.setYoutubeId(YOUTUBE_ID);
        when(mockRepo.findById(1L)).thenReturn(Optional.of(lesson));

        TranscriptController controller = new TranscriptController(mockRepo, mockService);

        // Act
        ResponseEntity<List<TranscriptSegment>> response = controller.getTranscript(1L);

        // ASSERT: should return HTTP 503 when service cannot fetch transcript
        // ON UNFIXED CODE: this FAILS — controller returns HTTP 200 with []
        // This confirms the missing error handling described in Requirement 1.4
        assertThat(response.getStatusCode().value())
                .as("TranscriptController should return HTTP 503 when service returns [] due to error "
                        + "— FAILS on unfixed code, confirming missing error handling")
                .isEqualTo(503);
    }
}
