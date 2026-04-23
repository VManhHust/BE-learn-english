package com.example.belearnenglish.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private Long userId;
    private String email;
    private String role;
}
