package com.example.belearnenglish.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import com.example.belearnenglish.entity.enums.ProPlan;

@Data
@Builder
public class ProStatusResponse {
    private boolean pro;
    private ProPlan currentPlanCode;
    private String currentPlanName;
    private Instant proStartsAt;
    private Instant proExpiresAt;
}
