package com.example.belearnenglish.dto;

public record RateLimitErrorResponse(String error, int retryAfter) {}
