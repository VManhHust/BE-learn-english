package com.example.belearnenglish.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface GoogleOAuthService {
    String buildAuthorizationUrl();
    GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class GoogleUserInfo {
        private String email;
        private String name;
        private String googleId;
    }
}
