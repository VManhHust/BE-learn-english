package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ErrorResponse;
import com.example.belearnenglish.dto.LoginRequest;
import com.example.belearnenglish.dto.LoginResponse;
import com.example.belearnenglish.dto.RegisterRequest;
import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.repository.UserRepository;
import com.example.belearnenglish.service.AuthService;
import com.example.belearnenglish.service.GoogleOAuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "linguaflow_refresh_token";

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AuthController(AuthService authService,
                          GoogleOAuthService googleOAuthService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.googleOAuthService = googleOAuthService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
        try {
            TokenPair tokenPair = authService.refresh(refreshToken);
            return ResponseEntity.ok(tokenPair);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleOAuthService.buildAuthorizationUrl());
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            HttpServletResponse response) throws IOException {
        if (error != null || code == null) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
            return;
        }
        try {
            GoogleOAuthService.GoogleUserInfo userInfo = googleOAuthService.exchangeCodeForUserInfo(code);
            User user = userRepository.findByEmail(userInfo.email())
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(userInfo.email())
                                .displayName(userInfo.name())
                                .googleId(userInfo.googleId())
                                .build();
                        return userRepository.save(newUser);
                    });
            TokenPair tokenPair = authService.generateTokenPair(user);
            response.sendRedirect(frontendUrl + "/auth/callback?accessToken=" + tokenPair.accessToken()
                    + "&refreshToken=" + tokenPair.refreshToken());
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
        }
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
