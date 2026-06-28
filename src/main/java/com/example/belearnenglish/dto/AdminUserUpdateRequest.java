package com.example.belearnenglish.dto;

import com.example.belearnenglish.entity.enums.Role;
import lombok.Data;

import java.time.Instant;

@Data
public class AdminUserUpdateRequest {
    private String displayName;
    private Role role;
    private Instant proStartsAt;
    private Instant proExpiresAt;
}
