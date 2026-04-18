package com.example.belearnenglish.security;

public interface JwtProvider {
    String generateAccessToken(Long userId, String email, String role);
    String generateRefreshToken(Long userId);
    JwtClaims validateToken(String token); // throws JwtException if invalid/expired
}
