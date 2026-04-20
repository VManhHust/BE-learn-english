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
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class YouTubeTranscriptService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeTranscriptService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TranscriptSegment> getTranscript(String youtubeId) {
        if (youtubeId == null || youtubeId.isBlank()) {
            return Collections.emptyList();
        }
        try {
            // Strategy 1: Innertube API (YouTube mobile/TV client — bypasses bot detection)
            List<TranscriptSegment> segments = fetchViaInnertube(youtubeId);
            if (!segments.isEmpty()) return segments;

            // Strategy 2: Direct timedtext with multiple langs
            segments = fetchViaTimedText(youtubeId);
            if (!segments.isEmpty()) return segments;

            log.warn("No transcript found for videoId={}", youtubeId);
            return Collections.emptyList();

        } catch (TranscriptFetchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching transcript for videoId={}: {}", youtubeId, e.getMessage());
            throw new TranscriptFetchException("Không thể tải transcript cho video: " + youtubeId, e);
        }
    }

    /**
     * Use YouTube Innertube API (used by YouTube TV/Android) to get transcript.
     * This bypasses bot detection because it uses the official internal API.
     */
    private List<TranscriptSegment> fetchViaInnertube(String youtubeId) {
        try {
            // Step 1: Get video page using TV client (less bot detection)
            String captionUrl = fetchCaptionUrlViaInnertubePlayer(youtubeId);
            if (captionUrl != null) {
                List<TranscriptSegment> segments = fetchXmlFromUrl(captionUrl);
                if (!segments.isEmpty()) {
                    log.info("Fetched {} segments via Innertube player for videoId={}", segments.size(), youtubeId);
                    return segments;
                }
            }
        } catch (Exception e) {
            log.debug("Innertube strategy failed for videoId={}: {}", youtubeId, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Call YouTube Innertube /player endpoint with TVHTML5 client to get captions.
     * This client is less restricted than web browser client.
     */
    private String fetchCaptionUrlViaInnertubePlayer(String youtubeId) {
        try {
            String url = "https://www.youtube.com/youtubei/v1/player?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";

            String body = "{"
                    + "\"videoId\":\"" + youtubeId + "\","
                    + "\"context\":{"
                    + "  \"client\":{"
                    + "    \"clientName\":\"TVHTML5\","
                    + "    \"clientVersion\":\"7.20230405.08.01\","
                    + "    \"hl\":\"en\","
                    + "    \"gl\":\"US\""
                    + "  }"
                    + "}"
                    + "}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/537.36");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Origin", "https://www.youtube.com");
            headers.set("Referer", "https://www.youtube.com/watch?v=" + youtubeId);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) return null;

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode captionTracks = root.path("captions")
                    .path("playerCaptionsTracklistRenderer")
                    .path("captionTracks");

            if (!captionTracks.isArray() || captionTracks.isEmpty()) {
                log.debug("No captionTracks in Innertube response for videoId={}", youtubeId);
                return null;
            }

            // Prefer English track (including auto-generated)
            JsonNode bestTrack = null;
            for (JsonNode track : captionTracks) {
                String lang = track.path("languageCode").asText("");
                String kind = track.path("kind").asText("");
                if (lang.startsWith("en")) {
                    // Prefer manual over auto-generated, but accept both
                    if (bestTrack == null || kind.equals("asr") && !bestTrack.path("kind").asText("").equals("asr")) {
                        bestTrack = track;
                    }
                    if (!kind.equals("asr")) break; // manual EN found, stop
                }
            }
            // Fallback: any track
            if (bestTrack == null) bestTrack = captionTracks.get(0);

            String baseUrl = bestTrack.path("baseUrl").asText(null);
            if (baseUrl != null) {
                log.info("Found caption track (lang={}, kind={}) via Innertube for videoId={}",
                        bestTrack.path("languageCode").asText(), bestTrack.path("kind").asText(), youtubeId);
                return baseUrl + "&fmt=xml";
            }

        } catch (Exception e) {
            log.debug("Innertube player API failed for videoId={}: {}", youtubeId, e.getMessage());
        }
        return null;
    }

    private List<TranscriptSegment> fetchXmlFromUrl(String url) {
        try {
            HttpHeaders headers = buildBrowserHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTranscriptXml(response.getBody());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch XML from {}: {}", url, e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<TranscriptSegment> fetchViaTimedText(String youtubeId) {
        String[] langs = {"en", "en-US", "en-GB"};
        for (String lang : langs) {
            try {
                String url = "https://www.youtube.com/api/timedtext?v=" + youtubeId
                        + "&lang=" + lang + "&fmt=xml";
                List<TranscriptSegment> segments = fetchXmlFromUrl(url);
                if (!segments.isEmpty()) {
                    log.info("Fetched {} segments via timedtext lang={} for videoId={}", segments.size(), lang, youtubeId);
                    return segments;
                }
            } catch (Exception e) {
                log.debug("timedtext lang={} failed for videoId={}: {}", lang, youtubeId, e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    private HttpHeaders buildBrowserHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return headers;
    }

    private List<TranscriptSegment> parseTranscriptXml(String xml) {
        List<TranscriptSegment> segments = new ArrayList<>();
        if (xml == null || xml.isBlank()) return segments;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList texts = doc.getElementsByTagName("text");
            for (int i = 0; i < texts.getLength(); i++) {
                Element el = (Element) texts.item(i);
                double start = Double.parseDouble(el.getAttribute("start"));
                double dur = el.hasAttribute("dur") ? Double.parseDouble(el.getAttribute("dur")) : 2.0;
                String text = el.getTextContent()
                        .replace("&amp;", "&").replace("&lt;", "<")
                        .replace("&gt;", ">").replace("&quot;", "\"")
                        .replace("&#39;", "'").replaceAll("<[^>]+>", "").trim();
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
