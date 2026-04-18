package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.TranscriptSegment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

@Service
public class YouTubeTranscriptService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<TranscriptSegment> getTranscript(String youtubeId) {
        try {
            // Fetch YouTube page to get caption track URL
            String pageUrl = "https://www.youtube.com/watch?v=" + youtubeId;
            String pageHtml = fetchUrl(pageUrl);

            // Extract caption URL from page
            String captionUrl = extractCaptionUrl(pageHtml, youtubeId);
            if (captionUrl == null) {
                return Collections.emptyList();
            }

            // Fetch XML transcript
            String xmlContent = fetchUrl(captionUrl);
            return parseTranscriptXml(xmlContent);

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String extractCaptionUrl(String html, String videoId) {
        // Try to find timedtext URL in page source
        Pattern pattern = Pattern.compile("\"captionTracks\":\\[\\{\"baseUrl\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String url = matcher.group(1).replace("\\u0026", "&");
            return url;
        }

        // Fallback: use direct timedtext API
        return "https://www.youtube.com/api/timedtext?v=" + videoId + "&lang=en&fmt=xml";
    }

    private String fetchUrl(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private List<TranscriptSegment> parseTranscriptXml(String xml) {
        List<TranscriptSegment> segments = new ArrayList<>();
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
            // Return empty if parse fails
        }
        return segments;
    }
}
