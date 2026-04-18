package com.example.belearnenglish.service;

public interface GoogleOAuthService {
    String buildAuthorizationUrl();
    GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode);

    record GoogleUserInfo(String email, String name, String googleId) {}
}
