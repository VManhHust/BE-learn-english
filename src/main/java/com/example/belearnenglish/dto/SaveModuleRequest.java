package com.example.belearnenglish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveModuleRequest {
    private Integer timeStartMs;
    private Integer timeEndMs;
    private String content;
}
