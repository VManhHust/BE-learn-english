package com.example.belearnenglish.dto;

public record SaveModuleRequest(
        Integer timeStartMs,
        Integer timeEndMs,
        String content
) {}
