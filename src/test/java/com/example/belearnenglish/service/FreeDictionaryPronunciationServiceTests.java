package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.VocabularyDeckDetailResponse.WordCardDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FreeDictionaryPronunciationServiceTests {

    @Test
    void enrichesMissingUsAndUkAudioWithoutCredentials() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FreeDictionaryPronunciationService service = new FreeDictionaryPronunciationService(
            builder, "https://api.dictionaryapi.dev"
        );

        server.expect(requestTo("https://api.dictionaryapi.dev/api/v2/entries/en/siblings"))
            .andRespond(withSuccess(response(), MediaType.APPLICATION_JSON));

        WordCardDto enriched = service.enrich(cardWithoutAudio());

        assertThat(enriched.audioUsUrl()).isEqualTo("https://audio.example/siblings_us_1.mp3");
        assertThat(enriched.audioUkUrl()).isEqualTo("https://audio.example/siblings_gb_1.mp3");
        assertThat(enriched.ipaUs()).isEqualTo("/existing-us/");
        server.verify();
    }

    private WordCardDto cardWithoutAudio() {
        return new WordCardDto(
            1L, "siblings", "Noun", "/existing-us/", null, null, null,
            "Brothers and sisters", "Anh chị em", "anh chị em", null, null,
            null, 1, "NEW"
        );
    }

    private String response() {
        return """
            [{
              "word": "siblings",
              "phonetics": [
                {"text": "/ˈsɪblɪŋz/", "audio": "//audio.example/siblings_gb_1.mp3"},
                {"text": "/ˈsɪblɪŋz/", "audio": "https://audio.example/siblings_us_1.mp3"}
              ]
            }]
            """;
    }
}
