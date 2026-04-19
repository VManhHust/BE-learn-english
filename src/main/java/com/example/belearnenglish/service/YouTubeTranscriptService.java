package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.TranscriptSegment;
import com.example.belearnenglish.exception.TranscriptFetchException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.util.*;

@Service
public class YouTubeTranscriptService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeTranscriptService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // YouTube Innertube API — used by YouTube mobile apps, reliable and no bot detection
    private static final String INNERTUBE_URL =
            "https://www.youtube.com/youtubei/v1/get_transcript?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";

    public List<TranscriptSegment> getTranscript(String youtubeId) {
        if (youtubeId == null || youtubeId.isBlank()) {
            return Collections.emptyList();
        }

        try {
            // Step 1: Get video page to extract required params (visitorData, etc.)
            String params = fetchTranscriptParams(youtubeId);
            if (params == null) {
                log.warn("Could not extract transcript params for videoId={}", youtubeId);
                // Fallback to timedtext API
                return fetchViaTimedText(youtubeId);
            }

            // Step 2: Call Innertube get_transcript endpoint
            List<TranscriptSegment> segments = fetchViaInnertube(youtubeId, params);
            if (!segments.isEmpty()) {
                return segments;
            }

            // Step 3: Fallback to timedtext
            return fetchViaTimedText(youtubeId);

        } catch (TranscriptFetchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching transcript for videoId={}: {}", youtubeId, e.getMessage());
            throw new TranscriptFetchException("Không thể tải transcript cho video: " + youtubeId, e);
        }
    }

    /**
     * Fetch transcript using YouTube's timedtext API with multiple language fallbacks.
     */
    private List<TranscriptSegment> fetchViaTimedText(String youtubeId) {
        String[] langs = {"en", "en-US", "en-GB"};
        for (String lang : langs) {
            try {
                String url = "https://www.youtube.com/api/timedtext?v=" + youtubeId
                        + "&lang=" + lang + "&fmt=xml&xorb=2&xobt=3&xovt=3";
                HttpHeaders headers = buildBrowserHeaders();
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null
                        && !response.getBody().isBlank()
                        && response.getBody().contains("<text")) {
                    List<TranscriptSegment> segments = parseTranscriptXml(response.getBody());
                    if (!segments.isEmpty()) {
                        log.info("Fetched {} segments via timedtext (lang={}) for videoId={}", segments.size(), lang, youtubeId);
                        return segments;
                    }
                }
            } catch (Exception e) {
                log.debug("timedtext lang={} failed for videoId={}: {}", lang, youtubeId, e.getMessage());
            }
        }
        log.warn("No transcript found via timedtext for videoId={}", youtubeId);
        return Collections.emptyList();
    }

    /**
     * Extract the serialized transcript params from the YouTube video page.
     * These params are needed for the Innertube get_transcript call.
     */
    private String fetchTranscriptParams(String youtubeId) {
        try {
            String pageUrl = "https://www.youtube.com/watch?v=" + youtubeId;
            HttpHeaders headers = buildBrowserHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(pageUrl, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }

            String html = response.getBody();

            // Extract serializedShareEntity or params for transcript
            int idx = html.indexOf("\"serializedShareEntity\":\"");
            if (idx == -1) idx = html.indexOf("\"params\":\"");
            if (idx == -1) return null;

            // Try to find captionTracks baseUrl directly
            int captionIdx = html.indexOf("\"captionTracks\":");
            if (captionIdx != -1) {
                int baseUrlIdx = html.indexOf("\"baseUrl\":\"", captionIdx);
                if (baseUrlIdx != -1) {
                    int start = baseUrlIdx + 11;
                    int end = html.indexOf("\"", start);
                    if (end > start) {
                        String rawUrl = html.substring(start, end).replace("\\u0026", "&");
                        log.info("Found captionTracks baseUrl for videoId={}", youtubeId);
                        return rawUrl; // Return the direct URL
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.debug("Failed to fetch page params for videoId={}: {}", youtubeId, e.getMessage());
            return null;
        }
    }

    /**
     * Fetch transcript via Innertube API using extracted params.
     * When params is a direct caption URL, fetch it directly.
     */
    private List<TranscriptSegment> fetchViaInnertube(String youtubeId, String params) {
        try {
            // If params looks like a URL (from captionTracks), fetch it directly
            if (params.startsWith("http")) {
                HttpHeaders headers = buildBrowserHeaders();
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(params, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<TranscriptSegment> segments = parseTranscriptXml(response.getBody());
                    if (!segments.isEmpty()) {
                        log.info("Fetched {} segments via captionTracks URL for videoId={}", segments.size(), youtubeId);
                        return segments;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Innertube fetch failed for videoId={}: {}", youtubeId, e.getMessage());
        }
        return Collections.emptyList();
    }

    private HttpHeaders buildBrowserHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Connection", "keep-alive");
        return headers;
    }

    private List<TranscriptSegment> parseTranscriptXml(String xml) {
        List<TranscriptSegment> segments = new ArrayList<>();
        if (xml == null || xml.isBlank()) return segments;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

            NodeList texts = doc.getElementsByTagName("text");
            for (int i = 0; i < texts.getLength(); i++) {
                Element el = (Element) texts.item(i);
                double start = Double.parseDouble(el.getAttribute("start"));
                double dur = el.hasAttribute("dur") ? Double.parseDouble(el.getAttribute("dur")) : 2.0;
                String text = el.getTextContent()
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'")
                        .replaceAll("<[^>]+>", "")
                        .trim();

                if (!text.isEmpty()) {
                    segments.add(new TranscriptSegment(i + 1, start, dur, text));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse transcript XML: {}", e.getMessage());
        }
        return segments;
    }
}
