package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.VocabularyDeckDetailResponse.WordCardDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class FreeDictionaryPronunciationService {

    private final RestClient restClient;

    public FreeDictionaryPronunciationService(
            RestClient.Builder restClientBuilder,
            @Value("${dictionary-api.base-url:https://api.dictionaryapi.dev}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public WordCardDto enrich(WordCardDto card) {
        if (card == null) {
            return null;
        }

        boolean needsUs = !StringUtils.hasText(card.audioUsUrl());
        boolean needsUk = !StringUtils.hasText(card.audioUkUrl());
        if (!needsUs && !needsUk) {
            return card;
        }

        Pronunciations pronunciations = lookup(card.word());
        Pronunciation us = needsUs ? pronunciations.us() : Pronunciation.EMPTY;
        Pronunciation uk = needsUk ? pronunciations.uk() : Pronunciation.EMPTY;

        return new WordCardDto(
            card.id(),
            card.word(),
            card.partOfSpeech(),
            firstNonBlank(card.ipaUs(), us.ipa()),
            firstNonBlank(card.ipaUk(), uk.ipa()),
            firstNonBlank(card.audioUsUrl(), us.audioUrl()),
            firstNonBlank(card.audioUkUrl(), uk.audioUrl()),
            card.englishDefinition(),
            card.vietnameseDefinition(),
            card.vietnameseTranslation(),
            card.exampleSentence(),
            card.exampleSentenceVi(),
            card.imageUrl(),
            card.sortOrder(),
            card.learningStatus()
        );
    }

    private Pronunciations lookup(String word) {
        try {
            JsonNode body = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/entries/en/{word}").build(word))
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(JsonNode.class);

            return parsePronunciations(body);
        } catch (RestClientException exception) {
            log.warn("Could not load DictionaryAPI.dev pronunciation for word={}: {}",
                word, exception.getMessage());
            return Pronunciations.EMPTY;
        }
    }

    private Pronunciations parsePronunciations(JsonNode body) {
        List<Pronunciation> available = new ArrayList<>();
        if (body != null && body.isArray()) {
            for (JsonNode entry : body) {
                JsonNode phonetics = entry.path("phonetics");
                if (!phonetics.isArray()) {
                    continue;
                }
                for (JsonNode phonetic : phonetics) {
                    String audioUrl = normalizeAudioUrl(textValue(phonetic, "audio"));
                    if (StringUtils.hasText(audioUrl)) {
                        available.add(new Pronunciation(textValue(phonetic, "text"), audioUrl));
                    }
                }
            }
        }

        Pronunciation us = findByAccent(available, Accent.US);
        Pronunciation uk = findByAccent(available, Accent.UK);
        if (us == Pronunciation.EMPTY && !available.isEmpty()) {
            us = available.getFirst();
        }
        if (uk == Pronunciation.EMPTY && available.size() > 1) {
            for (Pronunciation candidate : available) {
                if (candidate != us) {
                    uk = candidate;
                    break;
                }
            }
        }

        return new Pronunciations(us, uk);
    }

    private Pronunciation findByAccent(List<Pronunciation> pronunciations, Accent accent) {
        return pronunciations.stream()
            .filter(item -> accent.matches(item.audioUrl()))
            .findFirst()
            .orElse(Pronunciation.EMPTY);
    }

    private String normalizeAudioUrl(String audioUrl) {
        if (!StringUtils.hasText(audioUrl)) {
            return null;
        }
        return audioUrl.startsWith("//") ? "https:" + audioUrl : audioUrl;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value != null && value.isTextual() ? value.asText() : null;
    }

    private String firstNonBlank(String current, String fallback) {
        return StringUtils.hasText(current) ? current : fallback;
    }

    private enum Accent {
        US(List.of("_us_", "-us.", "/us/", "us_pron")),
        UK(List.of("_gb_", "_uk_", "-uk.", "/uk/", "uk_pron"));

        private final List<String> markers;

        Accent(List<String> markers) {
            this.markers = markers;
        }

        private boolean matches(String audioUrl) {
            String normalized = audioUrl.toLowerCase(Locale.ROOT);
            return markers.stream().anyMatch(normalized::contains);
        }
    }

    private record Pronunciation(String ipa, String audioUrl) {
        private static final Pronunciation EMPTY = new Pronunciation(null, null);
    }

    private record Pronunciations(Pronunciation us, Pronunciation uk) {
        private static final Pronunciations EMPTY = new Pronunciations(Pronunciation.EMPTY, Pronunciation.EMPTY);
    }
}
