package com.example.belearnenglish.dto;

public record LoginResponse(String accessToken, String refreshToken, UserDto user) {}
