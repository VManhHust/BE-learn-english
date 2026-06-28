package com.example.belearnenglish.dto;

import com.example.belearnenglish.entity.enums.ProPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProOrderRequest {

    @NotNull(message = "Plan is required")
    private ProPlan planCode;
}
