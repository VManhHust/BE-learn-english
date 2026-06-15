package com.example.belearnenglish.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProStatusResponse {
    private boolean pro;
    private Instant proExpiresAt;
}
