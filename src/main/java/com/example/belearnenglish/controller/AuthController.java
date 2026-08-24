package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ErrorResponse;
import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.repository.UserRepository;
import com.example.belearnenglish.service.AuthService;
import com.example.belearnenglish.service.GoogleOAuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "linguaflow_user_refresh_token";
    private static final long OAUTH_SESSION_CODE_TTL_SECONDS = 120;

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OAuthSession> pendingOAuthSessions = new ConcurrentHashMap<>();

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookies;

    public AuthController(AuthService authService,
                          GoogleOAuthService googleOAuthService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.googleOAuthService = googleOAuthService;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse servletResponse) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
        try {
            TokenPair tokenPair = authService.refresh(refreshToken);
            setRefreshCookie(servletResponse, tokenPair.getRefreshToken());
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
        String[] result = googleOAuthService.buildAuthorizationUrl();
        response.sendRedirect(result[0]);
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {
        if (!googleOAuthService.isValidState(state)) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
            return;
        }
        if (error != null || code == null) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
            return;
        }
        try {
            GoogleOAuthService.GoogleUserInfo userInfo = googleOAuthService.exchangeCodeForUserInfo(code);
            User user = userRepository.findByEmail(userInfo.getEmail())
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(userInfo.getEmail())
                                .displayName(userInfo.getName())
                                .googleId(userInfo.getGoogleId())
                                .build();
                        return userRepository.save(newUser);
            });
            TokenPair tokenPair = authService.generateTokenPair(user);
            setRefreshCookie(response, tokenPair.getRefreshToken());
            String sessionCode = createOAuthSessionCode(tokenPair);
            response.sendRedirect(frontendUrl
                    + "/auth/callback?code=" + encode(sessionCode));
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
        }
    }

    @PostMapping("/oauth/session")
    public ResponseEntity<?> exchangeOAuthSession(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.status(400).body(new ErrorResponse("OAuth session code is required"));
        }

        OAuthSession session = pendingOAuthSessions.remove(code);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(new ErrorResponse("OAuth session code invalid or expired"));
        }

        return ResponseEntity.ok(session.tokenPair());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String createOAuthSessionCode(TokenPair tokenPair) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = Instant.now();
        pendingOAuthSessions.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
        pendingOAuthSessions.put(code, new OAuthSession(
                tokenPair,
                now.plusSeconds(OAUTH_SESSION_CODE_TTL_SECONDS)
        ));
        return code;
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        response.addCookie(cookie);
    }

    private record OAuthSession(TokenPair tokenPair, Instant expiresAt) {
    }
}
