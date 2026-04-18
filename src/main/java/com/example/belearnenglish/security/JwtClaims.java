package com.example.belearnenglish.security;

public record JwtClaims(Long userId, String email, String role) {}
