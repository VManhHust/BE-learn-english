package com.example.belearnenglish.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GoogleOAuthServiceImpl(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    @Override
    public String buildAuthorizationUrl() {
        return GOOGLE_AUTH_URL
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=email+profile";
    }

    @Override
    public GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode) {
        try {
            String tokenRequestBody = "code=" + authorizationCode
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&redirect_uri=" + redirectUri
                    + "&grant_type=authorization_code";

            String tokenResponseBody = restClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(tokenRequestBody)
                    .retrieve()
                    .body(String.class);

            TokenResponse tokenResponse = objectMapper.readValue(tokenResponseBody, TokenResponse.class);

            String userInfoBody = restClient.get()
                    .uri(GOOGLE_USERINFO_URL)
                    .header("Authorization", "Bearer " + tokenResponse.accessToken())
                    .retrieve()
                    .body(String.class);

            UserInfoResponse userInfo = objectMapper.readValue(userInfoBody, UserInfoResponse.class);

            return new GoogleUserInfo(userInfo.email(), userInfo.name(), userInfo.id());
        } catch (Exception e) {
            throw new RuntimeException("oauth_failed", e);
        }
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType
    ) {}

    private record UserInfoResponse(
            @JsonProperty("id") String id,
            @JsonProperty("email") String email,
            @JsonProperty("name") String name
    ) {}
}
