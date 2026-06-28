package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ErrorResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenCookieController {

    private static final String REFRESH_COOKIE_NAME = "linguaflow_refresh_token";

    @PostMapping("/set-cookie")
    public ResponseEntity<?> setCookie(@RequestBody Map<String, String> request,
                                       HttpServletResponse response) {
        String refreshToken = request.getOrDefault("refreshToken", request.get("token"));
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("refreshToken is required"));
        }

        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
